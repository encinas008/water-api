package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.AttendanceEntity
import com.dreamsbo.posapi.persistence.entity.JobEntity
import com.dreamsbo.posapi.persistence.entity.PartnerEntity
import com.dreamsbo.posapi.persistence.repository.AttendanceRepository
import com.dreamsbo.posapi.persistence.repository.JobRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Service
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val jobRepository: JobRepository,
    private val partnerRepository: PartnerRepository,
) {

    fun getAttendanceByJob(jobId: UUID): List<AttendanceOutputDto> {
        val attendances = attendanceRepository.findByJobIdAndActive(jobId, true)
        return attendances.map { toAttendanceOutputDto(it) }
    }

    fun getAttendanceByPartner(partnerId: UUID): List<AttendanceOutputDto> {
        val attendances = attendanceRepository.findByPartnerIdAndActive(partnerId, true)
        return attendances.map { toAttendanceOutputDto(it) }
    }

    fun getAttendanceByJobAndDate(jobId: UUID, date: LocalDate): List<AttendanceOutputDto> {
        val attendances = attendanceRepository.findByJobIdAndAttendanceDateAndActive(jobId, date, true)
        return attendances.map { toAttendanceOutputDto(it) }
    }

    fun getAttendanceByJobAndDateRange(jobId: UUID, startDate: LocalDate, endDate: LocalDate): List<AttendanceOutputDto> {
        val attendances = attendanceRepository.findByJobIdAndDateRange(jobId, startDate, endDate, true)
        return attendances.map { toAttendanceOutputDto(it) }
    }

    fun getAttendanceGroupedByDate(jobId: UUID, startDate: LocalDate, endDate: LocalDate): List<AttendanceByDateDto> {
        val attendances = getAttendanceByJobAndDateRange(jobId, startDate, endDate)
        
        return attendances
            .groupBy { it.attendanceDate }
            .map { (date, attendanceList) ->
                AttendanceByDateDto(
                    attendanceDate = date,
                    attendances = attendanceList.sortedBy { it.partnerNumber ?: Long.MAX_VALUE }
                )
            }
            .sortedBy { it.attendanceDate }
    }

    @Transactional
    fun createAttendance(input: AttendanceInputDto): AttendanceOutputDto {
        val jobEntity = jobRepository.findById(input.jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = ${input.jobId}")
        }

        val partnerEntity = partnerRepository.findById(input.partnerId)
        if (partnerEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = ${input.partnerId}")
        }

        // Verificar si ya existe una asistencia para esta fecha
        val existingAttendance = attendanceRepository.findByJobIdAndPartnerIdAndAttendanceDateAndActive(
            input.jobId,
            input.partnerId,
            input.attendanceDate,
            true
        )

        if (existingAttendance.isPresent) {
            throw BadRequestException("Ya existe un registro de asistencia para este socio en esta fecha")
        }

        val attendance = AttendanceEntity(
            job = jobEntity.get(),
            partner = partnerEntity.get(),
            attendanceDate = input.attendanceDate,
            present = input.present,
            checkInTime = input.checkInTime,
            checkOutTime = input.checkOutTime
        )

        val savedAttendance = attendanceRepository.save(attendance)
        return toAttendanceOutputDto(savedAttendance)
    }

    @Transactional
    fun bulkCreateAttendance(input: BulkAttendanceInputDto): List<AttendanceOutputDto> {
        val jobEntity = jobRepository.findById(input.jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = ${input.jobId}")
        }

        val createdAttendances = mutableListOf<AttendanceOutputDto>()

        for (partnerAttendance in input.attendances) {
            val partnerEntity = partnerRepository.findById(partnerAttendance.partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            // Verificar si ya existe
            val existingAttendance = attendanceRepository.findByJobIdAndPartnerIdAndAttendanceDateAndActive(
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
                val updated = attendanceRepository.save(attendance)
                createdAttendances.add(toAttendanceOutputDto(updated))
            } else {
                // Crear nuevo
                val attendance = AttendanceEntity(
                    job = jobEntity.get(),
                    partner = partnerEntity.get(),
                    attendanceDate = input.attendanceDate,
                    present = partnerAttendance.present,
                    checkInTime = partnerAttendance.checkInTime,
                    checkOutTime = partnerAttendance.checkOutTime
                )
                val saved = attendanceRepository.save(attendance)
                createdAttendances.add(toAttendanceOutputDto(saved))
            }
        }

        return createdAttendances
    }

    @Transactional
    fun updateAttendance(id: UUID, input: AttendanceUpdateDto): AttendanceOutputDto {
        val attendanceEntity = attendanceRepository.findById(id)
        if (attendanceEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el registro de asistencia. AttendanceId = $id")
        }

        val attendance = attendanceEntity.get()

        input.present?.let { attendance.present = it }
        input.checkInTime?.let { attendance.checkInTime = it }
        input.checkOutTime?.let { attendance.checkOutTime = it }
        attendance.updatedAt = OffsetDateTime.now()

        val updatedAttendance = attendanceRepository.save(attendance)
        return toAttendanceOutputDto(updatedAttendance)
    }

    @Transactional
    fun deleteAttendance(id: UUID) {
        val attendanceEntity = attendanceRepository.findById(id)
        if (attendanceEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el registro de asistencia. AttendanceId = $id")
        }

        val attendance = attendanceEntity.get()
        attendance.active = false
        attendance.updatedAt = OffsetDateTime.now()
        attendanceRepository.save(attendance)
    }

    private fun toAttendanceOutputDto(entity: AttendanceEntity): AttendanceOutputDto {
        return AttendanceOutputDto(
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

