package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.MeetingAttendanceEntity
import com.dreamsbo.posapi.persistence.entity.MeetingEntity
import com.dreamsbo.posapi.persistence.entity.PartnerEntity
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
        if (meeting.locked || meeting.meetingDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se pueden registrar asistencias para esta reunión porque está bloqueada.")
        }

        val createdAttendances = mutableListOf<MeetingAttendanceOutputDto>()

        for (partnerAttendance in input.attendances) {
            val partnerEntity = partnerRepository.findById(partnerAttendance.partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            // Verificar si ya existe
            val existingAttendance = meetingAttendanceRepository.findByMeetingIdAndPartnerIdAndDate(
                input.meetingId,
                partnerAttendance.partnerId,
                input.attendanceDate,
                true
            )

            if (existingAttendance.isPresent) {
                // Actualizar existente
                val attendance = existingAttendance.get()
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
                
                val updated = meetingAttendanceRepository.save(attendance)
                createdAttendances.add(toMeetingAttendanceOutputDto(updated))
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
                
                val saved = meetingAttendanceRepository.save(attendance)
                createdAttendances.add(toMeetingAttendanceOutputDto(saved))
            }
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

        if (attendance.meeting.locked || attendance.meeting.meetingDate.plusDays(2).isBefore(LocalDate.now())) {
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
        return toMeetingAttendanceOutputDto(updatedAttendance)
    }

    @Transactional
    fun deleteAttendance(id: UUID) {
        val attendanceEntity = meetingAttendanceRepository.findById(id)
        if (attendanceEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el registro de asistencia. AttendanceId = $id")
        }

        val attendance = attendanceEntity.get()
        
        if (attendance.meeting.locked || attendance.meeting.meetingDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se puede eliminar esta asistencia porque la reunión está bloqueada.")
        }

        attendance.active = false
        attendance.updatedAt = OffsetDateTime.now()
        meetingAttendanceRepository.save(attendance)
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
        
        if (meeting.locked || meeting.meetingDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se pueden asignar socios a esta reunión porque está bloqueada.")
        }

        val meetingDate = meeting.meetingDate

        // Obtener todos los registros de asistencia activos para esta reunión
        val existingAttendances = meetingAttendanceRepository.findByMeetingIdAndActive(meetingId, true)

        // Desactivar registros de socios que no están en la nueva lista
        existingAttendances.forEach { attendance ->
            if (!input.partnerIds.contains(attendance.partner.id)) {
                attendance.active = false
                attendance.updatedAt = OffsetDateTime.now()
                meetingAttendanceRepository.save(attendance)
            }
        }

        // Crear o reactivar registros de asistencia para los socios seleccionados
        val newAttendances = mutableListOf<MeetingAttendanceEntity>()
        
        for (partnerId in input.partnerIds) {
            val partnerEntity = partnerRepository.findById(partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            val partner = partnerEntity.get()
            
            // Verificar si ya existe un registro de asistencia para este socio en esta fecha
            val existingAttendance = meetingAttendanceRepository.findByMeetingIdAndPartnerIdAndDate(
                meetingId,
                partnerId,
                meetingDate,
                true
            )
            
            if (existingAttendance.isPresent) {
                // Reactivar si estaba inactivo
                val attendance = existingAttendance.get()
                if (!attendance.active) {
                    attendance.active = true
                    attendance.updatedAt = OffsetDateTime.now()
                    meetingAttendanceRepository.save(attendance)
                }
                newAttendances.add(attendance)
            } else {
                // Crear nuevo registro de asistencia (por defecto ausente)
                val newAttendance = MeetingAttendanceEntity(
                    meeting = meeting,
                    partner = partner,
                    attendanceDate = meetingDate,
                    present = false // Por defecto ausente, se puede cambiar después
                )
                val saved = meetingAttendanceRepository.save(newAttendance)
                newAttendances.add(saved)
            }
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
            meetingAttendanceRepository.save(attendance)
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
}

