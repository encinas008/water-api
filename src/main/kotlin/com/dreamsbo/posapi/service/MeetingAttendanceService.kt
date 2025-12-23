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
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Service
class MeetingAttendanceService(
    private val meetingAttendanceRepository: MeetingAttendanceRepository,
    private val meetingRepository: MeetingRepository,
    private val partnerRepository: PartnerRepository,
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

        input.present?.let { attendance.present = it }
        input.checkInTime?.let { attendance.checkInTime = it }
        input.checkOutTime?.let { attendance.checkOutTime = it }
        attendance.updatedAt = OffsetDateTime.now()

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
        attendance.active = false
        attendance.updatedAt = OffsetDateTime.now()
        meetingAttendanceRepository.save(attendance)
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
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

