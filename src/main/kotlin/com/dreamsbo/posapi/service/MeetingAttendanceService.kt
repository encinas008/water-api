package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.MeetingAttendanceEntity
import com.dreamsbo.posapi.persistence.entity.MeetingEntity
import com.dreamsbo.posapi.persistence.entity.PartnerEntity
import com.dreamsbo.posapi.persistence.entity.WaterBillEntity
import com.dreamsbo.posapi.persistence.repository.MeetingAttendanceRepository
import com.dreamsbo.posapi.persistence.repository.MeetingRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Service
class MeetingAttendanceService(
    private val meetingAttendanceRepository: MeetingAttendanceRepository,
    private val meetingRepository: MeetingRepository,
    private val partnerRepository: PartnerRepository,
    private val billingConfigService: BillingConfigService,
    private val waterBillRepository: com.dreamsbo.posapi.persistence.repository.WaterBillRepository,
    private val billConceptItemRepository: com.dreamsbo.posapi.persistence.repository.BillConceptItemRepository
) {

    fun getAttendanceByMeeting(meetingId: UUID): List<MeetingAttendanceOutputDto> {
        val attendances = meetingAttendanceRepository.findByMeetingIdAndActive(meetingId, true)
        return attendances.map { toMeetingAttendanceOutputDto(it) }
    }

    fun getAttendanceByPartner(partnerId: UUID): List<MeetingAttendanceOutputDto> {
        val attendances = meetingAttendanceRepository.findByPartnerIdAndActive(partnerId, true)
        return attendances.map { toMeetingAttendanceOutputDto(it) }
    }

    fun getAttendanceByMeetingAndDate(meetingId: UUID, date: LocalDate): List<MeetingAttendanceOutputDto> {
        val attendances = meetingAttendanceRepository.findByMeetingIdAndAttendanceDateAndActive(meetingId, date, true)
        return attendances.map { toMeetingAttendanceOutputDto(it) }
    }

    @Transactional
    fun bulkCreateAttendance(input: BulkMeetingAttendanceInputDto): List<MeetingAttendanceOutputDto> {
        val meetingEntity = meetingRepository.findById(input.meetingId)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = ${input.meetingId}")
        }

        val meeting = meetingEntity.get()
        if (meeting.locked) {
            throw BadRequestException("No se pueden registrar asistencias para esta reunión porque está bloqueada.")
        }

        val createdAttendances = mutableListOf<MeetingAttendanceOutputDto>()

        for (partnerAttendance in input.attendances) {
            val partnerEntity = partnerRepository.findById(partnerAttendance.partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            // Verificar si ya existe (incluyendo inactivos)
            val existingAttendance = meetingAttendanceRepository.findByMeetingIdAndPartnerIdAndAttendanceDate(
                input.meetingId,
                partnerAttendance.partnerId,
                input.attendanceDate
            )

            val savedAttendance = if (existingAttendance.isPresent) {
                // Actualizar existente o reactivar
                val attendance = existingAttendance.get()
                attendance.active = true
                attendance.present = partnerAttendance.present
                attendance.checkInTime = partnerAttendance.checkInTime
                attendance.checkOutTime = partnerAttendance.checkOutTime
                attendance.updatedAt = OffsetDateTime.now()
                
                // Calcular multa por retraso si corresponde
                if (attendance.present && attendance.checkInTime != null) {
                    attendance.lateFine = calculateLateFine(meetingEntity.get(), attendance.checkInTime!!)
                } else {
                    attendance.lateFine = java.math.BigDecimal.ZERO
                }
                
                meetingAttendanceRepository.save(attendance)
            } else {
                // Crear nuevo
                val attendance = MeetingAttendanceEntity(
                    meeting = meetingEntity.get(),
                    partner = partnerEntity.get(),
                    attendanceDate = input.attendanceDate,
                    present = partnerAttendance.present,
                    checkInTime = partnerAttendance.checkInTime,
                    checkOutTime = partnerAttendance.checkOutTime
                )
                
                // Calcular multa por retraso si corresponde
                if (attendance.present && attendance.checkInTime != null) {
                    attendance.lateFine = calculateLateFine(meetingEntity.get(), attendance.checkInTime!!)
                }
                
                meetingAttendanceRepository.save(attendance)
            }
            
            syncMeetingFineWithBill(savedAttendance)
            createdAttendances.add(toMeetingAttendanceOutputDto(savedAttendance))
        }

        return createdAttendances
    }

    @Transactional
    fun updateAttendance(id: UUID, input: MeetingAttendanceUpdateDto): MeetingAttendanceOutputDto {
        val attendanceEntity = meetingAttendanceRepository.findById(id)
        if (attendanceEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el registro de asistencia. AttendanceId = $id")
        }

        val attendance = attendanceEntity.get()

        if (attendance.meeting.locked) {
            throw BadRequestException("No se puede editar esta asistencia porque la reunión está bloqueada.")
        }

        input.present?.let { attendance.present = it }
        input.checkInTime?.let { attendance.checkInTime = it }
        input.checkOutTime?.let { attendance.checkOutTime = it }
        attendance.updatedAt = OffsetDateTime.now()

        // Recalcular multa por retraso
        if (attendance.present && attendance.checkInTime != null) {
            attendance.lateFine = calculateLateFine(attendance.meeting, attendance.checkInTime!!)
        } else {
            attendance.lateFine = java.math.BigDecimal.ZERO
        }

        val updatedAttendance = meetingAttendanceRepository.save(attendance)
        syncMeetingFineWithBill(updatedAttendance)
        return toMeetingAttendanceOutputDto(updatedAttendance)
    }

    @Transactional
    fun deleteAttendance(id: UUID) {
        val attendanceEntity = meetingAttendanceRepository.findById(id)
        if (attendanceEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el registro de asistencia. AttendanceId = $id")
        }

        val attendance = attendanceEntity.get()
        
        if (attendance.meeting.locked) {
            throw BadRequestException("No se puede eliminar esta asistencia porque la reunión está bloqueada.")
        }

        attendance.active = false
        attendance.updatedAt = OffsetDateTime.now()
        val saved = meetingAttendanceRepository.save(attendance)
        syncMeetingFineWithBill(saved)
    }

    // Métodos para reemplazar funcionalidad de MeetingPartnerService
    
    fun getMeetingWithPartnerAssignments(meetingId: UUID): MeetingPartnerAssignmentDto {
        val meetingEntity = meetingRepository.findById(meetingId)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = $meetingId")
        }

        val meeting = meetingEntity.get()
        
        // Obtener todos los socios activos ordenados por partnerNumber ASC
        val allPartners = partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "partnerNumber"))
        
        // Obtener todos los registros de asistencia activos para esta reunión
        val allAttendances = meetingAttendanceRepository.findByMeetingIdAndActive(meetingId, true)
        val assignedPartnerIds = allAttendances.map { it.partner.id }.distinct().toSet()

        // Crear un mapa de partnerId -> primer attendanceId para acceso rápido
        val partnerToAttendanceIdMap = allAttendances
            .groupBy { it.partner.id }
            .mapValues { (_, attendances) -> attendances.minByOrNull { it.attendanceDate }?.id }

        // Crear lista de información de asignación
        val partnerAssignments = allPartners.map { partner ->
            val isAssigned = assignedPartnerIds.contains(partner.id)
            val assignmentId = if (isAssigned) {
                partnerToAttendanceIdMap[partner.id]
            } else {
                null
            }
            
            PartnerAssignmentInfoDto(
                partnerId = partner.id,
                partnerNumber = partner.partnerNumber,
                partnerName = partner.fullName,
                partnerIdentificationNumber = partner.partnerIdentificationNumber,
                isAssigned = isAssigned,
                assignmentId = assignmentId
            )
        }

        return MeetingPartnerAssignmentDto(
            meetingId = meeting.id,
            meetingName = meeting.name,
            assignedPartners = partnerAssignments
        )
    }

    @Transactional
    fun assignPartnersToMeeting(meetingId: UUID, input: AssignPartnersToMeetingDto): List<MeetingAttendanceOutputDto> {
        val meetingEntity = meetingRepository.findById(meetingId)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = $meetingId")
        }

        val meeting = meetingEntity.get()
        
        if (meeting.locked) {
            throw BadRequestException("No se pueden asignar socios a esta reunión porque está bloqueada.")
        }

        val meetingDate = meeting.meetingDate
        val periodStart = meetingDate.withDayOfMonth(1)

        // Optimización N+1: Pre-cargar todas las asistencias
        val allAttendances = meetingAttendanceRepository.findByMeetingId(meetingId)
        val attendancesByPartnerId = allAttendances.associateBy { it.partner.id }

        // Optimización N+1: Pre-cargar todos los socios
        val allPartnerIds = input.partnerIds + allAttendances.map { it.partner.id }
        val partnersById = partnerRepository.findAllById(allPartnerIds).associateBy { it.id }

        // Optimización N+1: Pre-cargar facturas pendientes
        val pendingBills = waterBillRepository.findPendingOrPartialPaidBillsForPartnersInPeriod(allPartnerIds.toList(), periodStart)
        val pendingBillsByPartnerId = pendingBills.associateBy { it.partner.id }

        val attendancesToSave = mutableListOf<MeetingAttendanceEntity>()

        // Desactivar registros de socios que no están en la nueva lista
        val existingActiveAttendances = allAttendances.filter { it.active }
        existingActiveAttendances.forEach { attendance ->
            if (!input.partnerIds.contains(attendance.partner.id)) {
                attendance.active = false
                attendance.updatedAt = OffsetDateTime.now()
                attendancesToSave.add(attendance)
            }
        }

        // Crear o reactivar registros de asistencia para los socios seleccionados
        val newAttendances = mutableListOf<MeetingAttendanceEntity>()
        
        for (partnerId in input.partnerIds) {
            val partner = partnersById[partnerId]
            if (partner == null) continue 

            val existingAttendance = attendancesByPartnerId[partnerId]
            
            if (existingAttendance != null && existingAttendance.attendanceDate == meetingDate) {
                // Reactivar si estaba inactivo
                if (!existingAttendance.active) {
                    existingAttendance.active = true
                    existingAttendance.present = false // Por defecto ausente al asignar
                    existingAttendance.updatedAt = OffsetDateTime.now()
                    attendancesToSave.add(existingAttendance)
                }
                newAttendances.add(existingAttendance)
            } else {
                // Crear nuevo registro
                val newAttendance = MeetingAttendanceEntity(
                    meeting = meeting,
                    partner = partner,
                    attendanceDate = meetingDate,
                    present = false
                )
                attendancesToSave.add(newAttendance)
                newAttendances.add(newAttendance)
            }
        }

        // Guardar asistencias en batch
        val savedAttendances = meetingAttendanceRepository.saveAll(attendancesToSave)

        // Sincronizar multas usando caché
        savedAttendances.forEach { savedAttendance ->
            val prefetchedBill = pendingBillsByPartnerId[savedAttendance.partner.id]
            syncMeetingFineWithBill(savedAttendance, prefetchedBill, true)
        }

        return newAttendances.map { toMeetingAttendanceOutputDto(it) }
    }

    @Transactional
    fun removePartnerFromMeeting(meetingId: UUID, partnerId: UUID) {
        // Desactivar todos los registros de asistencia de este socio para esta reunión
        val attendances = meetingAttendanceRepository.findByMeetingIdAndActive(meetingId, true)
            .filter { it.partner.id == partnerId }
        
        attendances.forEach { attendance ->
            attendance.active = false
            attendance.updatedAt = OffsetDateTime.now()
            val saved = meetingAttendanceRepository.save(attendance)
            syncMeetingFineWithBill(saved)
        }
    }

    private fun toMeetingAttendanceOutputDto(entity: MeetingAttendanceEntity): MeetingAttendanceOutputDto {
        return MeetingAttendanceOutputDto(
            id = entity.id,
            meetingId = entity.meeting.id,
            meetingName = entity.meeting.name,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            partnerNumber = entity.partner.partnerNumber,
            partnerIdentificationNumber = entity.partner.partnerIdentificationNumber,
            attendanceDate = entity.attendanceDate,
            present = entity.present,
            checkInTime = entity.checkInTime,
            checkOutTime = entity.checkOutTime,
            lateFine = entity.lateFine,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun calculateLateFine(meeting: MeetingEntity, checkInTime: OffsetDateTime): java.math.BigDecimal {
        val meetingDate = meeting.meetingDate
        var scheduledHour = meeting.hour
        if (meeting.amPm == "PM" && scheduledHour < 12) scheduledHour += 12
        if (meeting.amPm == "AM" && scheduledHour == 12) scheduledHour = 0
        
        val scheduledTime = OffsetDateTime.of(
            meetingDate.year, meetingDate.monthValue, meetingDate.dayOfMonth,
            scheduledHour, meeting.minute, 0, 0, checkInTime.offset
        )
        
        val gracePeriodEnd = scheduledTime.plusMinutes(meeting.waitingMinutes.toLong())
        
        return if (checkInTime.isAfter(gracePeriodEnd)) {
            val configKey = when (meeting.meetingType?.code) {
                "AULL" -> "MULTA_RETRASO_AULL"
                "CLASSIC" -> "MULTA_RETRASO_CLASICO"
                else -> null
            }
            
            if (configKey != null) {
                billingConfigService.getConfigValue(configKey, java.math.BigDecimal("5.0"))
            } else {
                java.math.BigDecimal.ZERO
            }
        } else {
            java.math.BigDecimal.ZERO
        }
    }

    private fun syncMeetingFineWithBill(
        attendance: MeetingAttendanceEntity,
        prefetchedBill: WaterBillEntity? = null,
        usePrefetchedBill: Boolean = false
    ) {
        val partner = attendance.partner
        val meeting = attendance.meeting
        val isAbsent = !attendance.present && attendance.active
        val hasLateFine = attendance.present && attendance.lateFine > java.math.BigDecimal.ZERO && attendance.active

        // Buscar factura PENDING o PARTIAL_PAID del mismo mes
        val periodStart = attendance.attendanceDate.withDayOfMonth(1)
        
        val bill = if (usePrefetchedBill) {
            prefetchedBill
        } else {
            val bills = waterBillRepository.findByPartnerIdAndActive(partner.id, true, Sort.unsorted())
            bills.firstOrNull { 
                it.billingPeriodStart == periodStart && 
                it.status.code == "PENDING" 
            }
        }

        if (bill != null) {
            val fineDate = formatDateLiteral(attendance.attendanceDate)
            
            // Si es falta, se usa la multa de la reunión. Si es retraso, se usa lateFine.
            val typeExtra = if (hasLateFine) " (RETRASO)" else ""
            val conceptName = "Multa Reunión: ${meeting.name}$typeExtra ($fineDate)"
            
            val existingConcepts = billConceptItemRepository.findByWaterBillIdAndActive(bill.id, true)
            // Tenemos que buscar si hay un concepto existente de esta reunión para este socio
            val conceptPrefix = "Multa Reunión: ${meeting.name}"
            val existingConcept = existingConcepts.firstOrNull { it.conceptName.startsWith(conceptPrefix) && it.conceptName.contains(fineDate) }

            val shouldHaveFine = isAbsent || hasLateFine
            val expectedAmount = if (isAbsent) meeting.fine else if (hasLateFine) attendance.lateFine else java.math.BigDecimal.ZERO
            
            if (shouldHaveFine && expectedAmount > java.math.BigDecimal.ZERO) {
                if (existingConcept == null) {
                    // Agregar nueva multa
                    val newConcept = com.dreamsbo.posapi.persistence.entity.BillConceptItemEntity(
                        waterBill = bill,
                        conceptName = conceptName,
                        assignedDate = attendance.attendanceDate,
                        amount = expectedAmount
                    )
                    billConceptItemRepository.save(newConcept)
                    
                    bill.totalAmount = bill.totalAmount.add(expectedAmount)
                    bill.remainingBalance = bill.remainingBalance.add(expectedAmount)
                    waterBillRepository.save(bill)
                    
                    partner.currentDebt = partner.currentDebt.add(expectedAmount)
                    partnerRepository.save(partner)
                } else if (existingConcept.conceptName != conceptName || existingConcept.amount != expectedAmount) {
                    // Actualizar multa existente (cambió de falta a retraso o monto)
                    val oldAmount = existingConcept.amount
                    
                    existingConcept.conceptName = conceptName
                    existingConcept.amount = expectedAmount
                    billConceptItemRepository.save(existingConcept)
                    
                    val difference = expectedAmount.subtract(oldAmount)
                    bill.totalAmount = bill.totalAmount.add(difference)
                    bill.remainingBalance = bill.remainingBalance.add(difference)
                    waterBillRepository.save(bill)
                    
                    partner.currentDebt = partner.currentDebt.add(difference)
                    partnerRepository.save(partner)
                }
            } else {
                // No debería tener multa (estuvo presente a tiempo, o desasignado)
                if (existingConcept != null) {
                    existingConcept.active = false
                    billConceptItemRepository.save(existingConcept)
                    
                    val fineAmount = existingConcept.amount
                    bill.totalAmount = bill.totalAmount.subtract(fineAmount)
                    bill.remainingBalance = bill.remainingBalance.subtract(fineAmount)
                    waterBillRepository.save(bill)
                    
                    partner.currentDebt = partner.currentDebt.subtract(fineAmount)
                    partnerRepository.save(partner)
                }
            }
        }
    }

    private fun formatDateLiteral(date: LocalDate): String {
        val day = String.format("%02d", date.dayOfMonth)
        val monthName = getMonthName(date.monthValue).replaceFirstChar { it.uppercase() }
        val year = date.year
        return "$day/$monthName/$year"
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "enero"
            2 -> "febrero"
            3 -> "marzo"
            4 -> "abril"
            5 -> "mayo"
            6 -> "junio"
            7 -> "julio"
            8 -> "agosto"
            9 -> "septiembre"
            10 -> "octubre"
            11 -> "noviembre"
            12 -> "diciembre"
            else -> ""
        }
    }
}

