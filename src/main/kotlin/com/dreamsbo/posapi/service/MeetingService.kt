package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.MeetingInputDto
import com.dreamsbo.posapi.dto.MeetingOutputDto
import com.dreamsbo.posapi.dto.MeetingUpdateDto
import com.dreamsbo.posapi.persistence.entity.MeetingEntity
import com.dreamsbo.posapi.persistence.entity.MeetingPartnerEntity
import com.dreamsbo.posapi.persistence.repository.MeetingRepository
import com.dreamsbo.posapi.persistence.repository.MeetingTypeRepository
import com.dreamsbo.posapi.persistence.repository.MeetingPartnerRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val meetingTypeRepository: MeetingTypeRepository,
    private val partnerRepository: PartnerRepository,
    private val meetingPartnerRepository: MeetingPartnerRepository
) {

    fun findAll(): List<MeetingOutputDto> {
        return meetingRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt"))
            .map { toMeetingOutputDto(it) }
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
            name = input.name,
            meetingDate = input.meetingDate,
            hour = input.hour,
            minute = input.minute,
            amPm = input.amPm.uppercase(),
            meetingType = meetingType,
            description = input.description ?: "",
            fine = input.fine
        )
        val savedMeeting = meetingRepository.save(meeting)
        
        // Si es una reunión CLASSIC, asignar automáticamente todos los socios activos
        if (meetingType?.code == "CLASSIC") {
            assignAllPartnersToClassicMeeting(savedMeeting)
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

        input.name?.let { meeting.name = it }
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
    private fun assignAllPartnersToClassicMeeting(meeting: MeetingEntity) {
        // Obtener todos los socios activos
        val allPartners = partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "partnerNumber"))
        
        // Crear asignaciones para todos los socios
        allPartners.forEach { partner ->
            val meetingPartner = MeetingPartnerEntity(
                meeting = meeting,
                partner = partner
            )
            meetingPartnerRepository.save(meetingPartner)
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
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            active = entity.active
        )
    }
}

