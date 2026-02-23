package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.JobAttendanceEntity
import com.dreamsbo.posapi.persistence.entity.JobEntity
import com.dreamsbo.posapi.persistence.entity.PartnerEntity
import com.dreamsbo.posapi.persistence.repository.JobAttendanceRepository
import com.dreamsbo.posapi.persistence.repository.JobRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Service
class JobAttendanceService(
    private val jobAttendanceRepository: JobAttendanceRepository,
    private val jobRepository: JobRepository,
    private val partnerRepository: PartnerRepository,
) {

    fun getAttendanceByJob(jobId: UUID): List<JobAttendanceOutputDto> {
        val attendances = jobAttendanceRepository.findByJobIdAndActive(jobId, true)
        return attendances.map { toJobAttendanceOutputDto(it) }
    }

    fun getAttendanceByPartner(partnerId: UUID): List<JobAttendanceOutputDto> {
        val attendances = jobAttendanceRepository.findByPartnerIdAndActive(partnerId, true)
        return attendances.map { toJobAttendanceOutputDto(it) }
    }

    fun getAttendanceByJobAndDate(jobId: UUID, date: LocalDate): List<JobAttendanceOutputDto> {
        val attendances = jobAttendanceRepository.findByJobIdAndAttendanceDateAndActive(jobId, date, true)
        return attendances.map { toJobAttendanceOutputDto(it) }
    }

    fun getAttendanceByJobAndDateRange(jobId: UUID, startDate: LocalDate, endDate: LocalDate): List<JobAttendanceOutputDto> {
        val attendances = jobAttendanceRepository.findByJobIdAndDateRange(jobId, startDate, endDate, true)
        return attendances.map { toJobAttendanceOutputDto(it) }
    }

    fun getAttendanceGroupedByDate(jobId: UUID, startDate: LocalDate, endDate: LocalDate): List<JobAttendanceByDateDto> {
        val attendances = getAttendanceByJobAndDateRange(jobId, startDate, endDate)
        
        return attendances
            .groupBy { it.attendanceDate }
            .map { (date, attendanceList) ->
                JobAttendanceByDateDto(
                    attendanceDate = date,
                    attendances = attendanceList.sortedBy { it.partnerNumber ?: Long.MAX_VALUE }
                )
            }
            .sortedBy { it.attendanceDate }
    }

    @Transactional
    fun createAttendance(input: JobAttendanceInputDto): JobAttendanceOutputDto {
        val jobEntity = jobRepository.findById(input.jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = ${input.jobId}")
        }

        val job = jobEntity.get()
        if (job.locked || job.startDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se pueden registrar asistencias para este trabajo porque está bloqueado.")
        }

        val partnerEntity = partnerRepository.findById(input.partnerId)
        if (partnerEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = ${input.partnerId}")
        }

        // Verificar si ya existe una asistencia para esta fecha
        val existingAttendance = jobAttendanceRepository.findByJobIdAndPartnerIdAndAttendanceDateAndActive(
            input.jobId,
            input.partnerId,
            input.attendanceDate,
            true
        )

        if (existingAttendance.isPresent) {
            throw BadRequestException("Ya existe un registro de asistencia para este socio en esta fecha")
        }

        val attendance = JobAttendanceEntity(
            job = jobEntity.get(),
            partner = partnerEntity.get(),
            attendanceDate = input.attendanceDate,
            present = input.present,
            checkInTime = input.checkInTime,
            checkOutTime = input.checkOutTime
        )

        val savedAttendance = jobAttendanceRepository.save(attendance)
        return toJobAttendanceOutputDto(savedAttendance)
    }

    @Transactional
    fun bulkCreateAttendance(input: BulkJobAttendanceInputDto): List<JobAttendanceOutputDto> {
        val jobEntity = jobRepository.findById(input.jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = ${input.jobId}")
        }

        val job = jobEntity.get()
        if (job.locked || job.startDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se pueden registrar asistencias para este trabajo porque está bloqueado.")
        }

        val createdAttendances = mutableListOf<JobAttendanceOutputDto>()

        for (partnerAttendance in input.attendances) {
            val partnerEntity = partnerRepository.findById(partnerAttendance.partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            // Verificar si ya existe
            val existingAttendance = jobAttendanceRepository.findByJobIdAndPartnerIdAndAttendanceDateAndActive(
                input.jobId,
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
                val updated = jobAttendanceRepository.save(attendance)
                createdAttendances.add(toJobAttendanceOutputDto(updated))
            } else {
                // Crear nuevo
                val attendance = JobAttendanceEntity(
                    job = jobEntity.get(),
                    partner = partnerEntity.get(),
                    attendanceDate = input.attendanceDate,
                    present = partnerAttendance.present,
                    checkInTime = partnerAttendance.checkInTime,
                    checkOutTime = partnerAttendance.checkOutTime
                )
                val saved = jobAttendanceRepository.save(attendance)
                createdAttendances.add(toJobAttendanceOutputDto(saved))
            }
        }

        return createdAttendances
    }

    @Transactional
    fun updateAttendance(id: UUID, input: JobAttendanceUpdateDto): JobAttendanceOutputDto {
        val attendanceEntity = jobAttendanceRepository.findById(id)
        if (attendanceEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el registro de asistencia. AttendanceId = $id")
        }

        val attendance = attendanceEntity.get()
        
        if (attendance.job.locked || attendance.job.startDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se puede editar esta asistencia porque el trabajo está bloqueado.")
        }

        input.present?.let { attendance.present = it }
        input.checkInTime?.let { attendance.checkInTime = it }
        input.checkOutTime?.let { attendance.checkOutTime = it }
        attendance.updatedAt = OffsetDateTime.now()

        val updatedAttendance = jobAttendanceRepository.save(attendance)
        return toJobAttendanceOutputDto(updatedAttendance)
    }

    @Transactional
    fun deleteAttendance(id: UUID) {
        val attendanceEntity = jobAttendanceRepository.findById(id)
        if (attendanceEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el registro de asistencia. AttendanceId = $id")
        }

        val attendance = attendanceEntity.get()
        
        if (attendance.job.locked || attendance.job.startDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se puede eliminar esta asistencia porque el trabajo está bloqueado.")
        }

        attendance.active = false
        attendance.updatedAt = OffsetDateTime.now()
        jobAttendanceRepository.save(attendance)
    }

    // Métodos para reemplazar funcionalidad de JobPartnerService
    
    fun getJobWithPartnerAssignments(jobId: UUID): JobPartnerAssignmentDto {
        val jobEntity = jobRepository.findById(jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = $jobId")
        }

        val job = jobEntity.get()
        
        // Obtener todos los socios activos ordenados por partnerNumber ASC
        val allPartners = partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "partnerNumber"))
        
        // Obtener todos los registros de asistencia activos para este trabajo
        val allAttendances = jobAttendanceRepository.findByJobIdAndActive(jobId, true)
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

        return JobPartnerAssignmentDto(
            jobId = job.id,
            jobName = job.name,
            assignedPartners = partnerAssignments
        )
    }

    @Transactional
    fun assignPartnersToJob(jobId: UUID, input: AssignPartnersToJobDto): List<JobAttendanceOutputDto> {
        val jobEntity = jobRepository.findById(jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = $jobId")
        }

        val job = jobEntity.get()
        
        if (job.locked || job.startDate.plusDays(2).isBefore(LocalDate.now())) {
            throw BadRequestException("No se pueden asignar socios a este trabajo porque está bloqueado.")
        }

        val jobStartDate = job.startDate

        // Obtener todos los registros de asistencia activos para este trabajo
        val existingAttendances = jobAttendanceRepository.findByJobIdAndActive(jobId, true)

        // Desactivar registros de socios que no están en la nueva lista
        existingAttendances.forEach { attendance ->
            if (!input.partnerIds.contains(attendance.partner.id)) {
                attendance.active = false
                attendance.updatedAt = OffsetDateTime.now()
                jobAttendanceRepository.save(attendance)
            }
        }

        // Crear o reactivar registros de asistencia para los socios seleccionados
        val newAttendances = mutableListOf<JobAttendanceEntity>()
        
        for (partnerId in input.partnerIds) {
            val partnerEntity = partnerRepository.findById(partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            val partner = partnerEntity.get()
            
            // Verificar si ya existe un registro de asistencia para este socio en esta fecha
            val existingAttendance = jobAttendanceRepository.findByJobIdAndPartnerIdAndAttendanceDateAndActive(
                jobId,
                partnerId,
                jobStartDate,
                true
            )
            
            if (existingAttendance.isPresent) {
                // Reactivar si estaba inactivo
                val attendance = existingAttendance.get()
                if (!attendance.active) {
                    attendance.active = true
                    attendance.updatedAt = OffsetDateTime.now()
                    jobAttendanceRepository.save(attendance)
                }
                newAttendances.add(attendance)
            } else {
                // Crear nuevo registro de asistencia (por defecto ausente)
                val newAttendance = JobAttendanceEntity(
                    job = job,
                    partner = partner,
                    attendanceDate = jobStartDate,
                    present = false // Por defecto ausente, se puede cambiar después
                )
                val saved = jobAttendanceRepository.save(newAttendance)
                newAttendances.add(saved)
            }
        }

        return newAttendances.map { toJobAttendanceOutputDto(it) }
    }

    @Transactional
    fun removePartnerFromJob(jobId: UUID, partnerId: UUID) {
        // Desactivar todos los registros de asistencia de este socio para este trabajo
        val attendances = jobAttendanceRepository.findByJobIdAndActive(jobId, true)
            .filter { it.partner.id == partnerId }
        
        attendances.forEach { attendance ->
            attendance.active = false
            attendance.updatedAt = OffsetDateTime.now()
            jobAttendanceRepository.save(attendance)
        }
    }

    private fun toJobAttendanceOutputDto(entity: JobAttendanceEntity): JobAttendanceOutputDto {
        return JobAttendanceOutputDto(
            id = entity.id,
            jobId = entity.job.id,
            jobName = entity.job.name,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            partnerNumber = entity.partner.partnerNumber,
            partnerIdentificationNumber = entity.partner.partnerIdentificationNumber,
            attendanceDate = entity.attendanceDate,
            present = entity.present,
            checkInTime = entity.checkInTime,
            checkOutTime = entity.checkOutTime,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

