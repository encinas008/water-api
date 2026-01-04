package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.MeetingInputDto
import com.dreamsbo.posapi.dto.MeetingOutputDto
import com.dreamsbo.posapi.dto.MeetingUpdateDto
import com.dreamsbo.posapi.persistence.entity.MeetingEntity
import com.dreamsbo.posapi.persistence.entity.MeetingAttendanceEntity
import com.dreamsbo.posapi.persistence.repository.MeetingRepository
import com.dreamsbo.posapi.persistence.repository.MeetingTypeRepository
import com.dreamsbo.posapi.persistence.repository.MeetingAttendanceRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val meetingTypeRepository: MeetingTypeRepository,
    private val partnerRepository: PartnerRepository,
    private val meetingAttendanceRepository: MeetingAttendanceRepository
) {

    fun findAll(): List<MeetingOutputDto> {
        return meetingRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt"))
            .map { toMeetingOutputDto(it) }
    }

    fun getScheduledDates(): List<String> {
        return meetingRepository.findDistinctMeetingDatesByActive(true).map { it.toString() }
    }

    fun findAllPaginated(page: Int, size: Int, search: String?, date: java.time.LocalDate? = null): Page<MeetingOutputDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        val meetingPage = when {
            !search.isNullOrBlank() && date != null -> {
                meetingRepository.findAllByActiveAndSearchAndMeetingDate(true, search.trim(), date, pageable)
            }
            !search.isNullOrBlank() -> {
                meetingRepository.findAllByActiveAndSearch(true, search.trim(), pageable)
            }
            date != null -> {
                meetingRepository.findAllByActiveAndMeetingDate(true, date, pageable)
            }
            else -> {
                meetingRepository.findAllByActive(true, pageable)
            }
        }
        
        return meetingPage.map { toMeetingOutputDto(it) }
    }

    fun findById(id: UUID): MeetingOutputDto {
        val meetingEntity = meetingRepository.findById(id)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = $id")
        }
        return toMeetingOutputDto(meetingEntity.get())
    }

    @Transactional
    fun createMeeting(input: MeetingInputDto): MeetingOutputDto {
        // Validar hora y minuto
        validateTime(input.hour, input.minute, input.amPm)

        val meetingType = input.meetingTypeCode?.let {
            meetingTypeRepository.findByCodeAndActive(it, true)
                .orElseThrow { NotFoundEntityException("Tipo de reunión no encontrado: $it") }
        }

        val meeting = MeetingEntity(
            name = input.name.uppercase().trim(),
            meetingDate = input.meetingDate,
            hour = input.hour,
            minute = input.minute,
            amPm = input.amPm.uppercase(),
            meetingType = meetingType,
            description = input.description ?: "",
            fine = input.fine,
            waitingMinutes = input.waitingMinutes
        )
        val savedMeeting = meetingRepository.save(meeting)
        
        // Si es una reunión CLASSIC, asignar automáticamente todos los socios activos
        if (meetingType?.code == "CLASSIC") {
            assignAllPartners(savedMeeting)
        }
        
        return toMeetingOutputDto(savedMeeting)
    }

    @Transactional
    fun updateMeeting(id: UUID, input: MeetingUpdateDto): MeetingOutputDto {
        val meetingEntity = meetingRepository.findById(id)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = $id")
        }

        val meeting = meetingEntity.get()

        input.name?.let { meeting.name = it.uppercase().trim() }
        input.meetingDate?.let { meeting.meetingDate = it }
        
        // Validar y actualizar hora si se proporciona
        if (input.hour != null || input.minute != null || input.amPm != null) {
            val hour = input.hour ?: meeting.hour
            val minute = input.minute ?: meeting.minute
            val amPm = input.amPm ?: meeting.amPm
            
            validateTime(hour, minute, amPm)
            
            meeting.hour = hour
            meeting.minute = minute
            meeting.amPm = amPm.uppercase()
        }
        
        input.meetingTypeCode?.let {
            meeting.meetingType = meetingTypeRepository.findByCodeAndActive(it, true)
                .orElseThrow { NotFoundEntityException("Tipo de reunión no encontrado: $it") }
        }
        
        input.description?.let { meeting.description = it }
        input.fine?.let { meeting.fine = it }
        input.waitingMinutes?.let { meeting.waitingMinutes = it }

        meeting.updatedAt = OffsetDateTime.now()

        val updatedMeeting = meetingRepository.save(meeting)
        return toMeetingOutputDto(updatedMeeting)
    }

    @Transactional
    fun deleteMeeting(id: UUID) {
        val meetingEntity = meetingRepository.findById(id)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = $id")
        }

        val meeting = meetingEntity.get()
        meeting.active = false
        meeting.updatedAt = OffsetDateTime.now()
        meetingRepository.save(meeting)
    }

    private fun validateTime(hour: Int, minute: Int, amPm: String) {
        if (hour < 1 || hour > 12) {
            throw IllegalArgumentException("La hora debe estar entre 1 y 12")
        }
        if (minute < 0 || minute > 59) {
            throw IllegalArgumentException("El minuto debe estar entre 0 y 59")
        }
        if (amPm.uppercase() !in listOf("AM", "PM")) {
            throw IllegalArgumentException("AM/PM debe ser 'AM' o 'PM'")
        }
        
        // Validar que no sea horario de madrugada (12 AM - 6 AM)
        if (amPm.uppercase() == "AM") {
            if (hour == 12 || (hour >= 1 && hour <= 6)) {
                throw IllegalArgumentException("No se permiten reuniones entre 12 AM y 6 AM")
            }
        }
        
        // Validar que en PM solo se permitan horas de 1 PM a 8 PM
        if (amPm.uppercase() == "PM") {
            if (hour < 1 || hour > 8) {
                throw IllegalArgumentException("En PM solo se permiten reuniones de 1 PM a 8 PM")
            }
        }
    }

    @Transactional
    fun assignAllPartners(meeting: MeetingEntity) {
        // Obtener todos los socios activos
        val allPartners = partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "partnerNumber"))
        
        // Crear registros de asistencia para todos los socios (por defecto ausentes)
        allPartners.forEach { partner ->
            // Verificar si ya existe un registro de asistencia
            val existingAttendance = meetingAttendanceRepository.findByMeetingIdAndPartnerIdAndDate(
                meeting.id,
                partner.id,
                meeting.meetingDate,
                true
            )
            
            if (existingAttendance.isEmpty) {
                val attendance = MeetingAttendanceEntity(
                    meeting = meeting,
                    partner = partner,
                    attendanceDate = meeting.meetingDate,
                    present = false // Por defecto ausente, se puede cambiar después
                )
                meetingAttendanceRepository.save(attendance)
            }
        }
    }

    @Transactional
    fun assignSelectedPartnersToMeeting(meeting: MeetingEntity, partnerIds: List<UUID>) {
        val selectedPartners = partnerRepository.findAllById(partnerIds)
        
        selectedPartners.forEach { partner ->
            val existingAttendance = meetingAttendanceRepository.findByMeetingIdAndPartnerIdAndDate(
                meeting.id,
                partner.id,
                meeting.meetingDate,
                true
            )
            
            if (existingAttendance.isEmpty) {
                val attendance = MeetingAttendanceEntity(
                    meeting = meeting,
                    partner = partner,
                    attendanceDate = meeting.meetingDate,
                    present = false 
                )
                meetingAttendanceRepository.save(attendance)
            }
        }
    }

    private fun toMeetingOutputDto(entity: MeetingEntity): MeetingOutputDto {
        return MeetingOutputDto(
            id = entity.id,
            name = entity.name,
            meetingDate = entity.meetingDate,
            hour = entity.hour,
            minute = entity.minute,
            amPm = entity.amPm,
            meetingTypeCode = entity.meetingType?.code,
            meetingTypeName = entity.meetingType?.name,
            description = entity.description,
            fine = entity.fine,
            waitingMinutes = entity.waitingMinutes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            active = entity.active
        )
    }
}

