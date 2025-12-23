package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.MeetingEntity
import com.dreamsbo.posapi.persistence.entity.MeetingPartnerEntity
import com.dreamsbo.posapi.persistence.entity.PartnerEntity
import com.dreamsbo.posapi.persistence.repository.MeetingPartnerRepository
import com.dreamsbo.posapi.persistence.repository.MeetingRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class MeetingPartnerService(
    private val meetingPartnerRepository: MeetingPartnerRepository,
    private val meetingRepository: MeetingRepository,
    private val partnerRepository: PartnerRepository,
) {

    fun getPartnersByMeetingId(meetingId: UUID): List<MeetingPartnerOutputDto> {
        val meetingPartners = meetingPartnerRepository.findByMeetingIdAndActive(meetingId, true)
        return meetingPartners.map { toMeetingPartnerOutputDto(it) }
    }

    fun getMeetingsByPartnerId(partnerId: UUID): List<MeetingPartnerOutputDto> {
        val meetingPartners = meetingPartnerRepository.findByPartnerIdAndActive(partnerId, true)
        return meetingPartners.map { toMeetingPartnerOutputDto(it) }
    }

    fun getMeetingWithPartnerAssignments(meetingId: UUID): MeetingPartnerAssignmentDto {
        val meetingEntity = meetingRepository.findById(meetingId)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = $meetingId")
        }

        val meeting = meetingEntity.get()
        
        // Obtener todos los socios activos ordenados por partnerNumber ASC
        val allPartners = partnerRepository.findAllByActive(true, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "partnerNumber"))
        
        // Obtener asignaciones existentes para esta reunión
        val existingAssignments = meetingPartnerRepository.findByMeetingIdAndActive(meetingId, true)
        val assignedPartnerIds = existingAssignments.map { it.partner.id }.toSet()

        // Crear lista de información de asignación
        val partnerAssignments = allPartners.map { partner ->
            val assignment = existingAssignments.find { it.partner.id == partner.id }
            PartnerAssignmentInfoDto(
                partnerId = partner.id,
                partnerNumber = partner.partnerNumber,
                partnerName = partner.fullName,
                partnerIdentificationNumber = partner.partnerIdentificationNumber,
                isAssigned = assignment != null,
                assignmentId = assignment?.id
            )
        }

        return MeetingPartnerAssignmentDto(
            meetingId = meeting.id,
            meetingName = meeting.name,
            assignedPartners = partnerAssignments
        )
    }

    @Transactional
    fun assignPartnersToMeeting(meetingId: UUID, input: AssignPartnersToMeetingDto): List<MeetingPartnerOutputDto> {
        val meetingEntity = meetingRepository.findById(meetingId)
        if (meetingEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado la reunión. MeetingId = $meetingId")
        }

        val meeting = meetingEntity.get()

        // Desactivar todas las asignaciones existentes
        val existingAssignments = meetingPartnerRepository.findByMeetingIdAndActive(meetingId, true)
        existingAssignments.forEach { assignment ->
            assignment.active = false
            meetingPartnerRepository.save(assignment)
        }

        // Crear nuevas asignaciones para los socios seleccionados
        val newAssignments = mutableListOf<MeetingPartnerEntity>()
        
        for (partnerId in input.partnerIds) {
            val partnerEntity = partnerRepository.findById(partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            val partner = partnerEntity.get()
            
            // Verificar si ya existe una asignación (aunque esté inactiva)
            val existingAssignment = meetingPartnerRepository.findByMeetingIdAndPartnerId(meetingId, partnerId)
            
            if (existingAssignment.isPresent) {
                // Reactivar la asignación existente
                val assignment = existingAssignment.get()
                assignment.active = true
                meetingPartnerRepository.save(assignment)
                newAssignments.add(assignment)
            } else {
                // Crear nueva asignación
                val newAssignment = MeetingPartnerEntity(
                    meeting = meeting,
                    partner = partner
                )
                val saved = meetingPartnerRepository.save(newAssignment)
                newAssignments.add(saved)
            }
        }

        return newAssignments.map { toMeetingPartnerOutputDto(it) }
    }

    @Transactional
    fun removePartnerFromMeeting(meetingId: UUID, partnerId: UUID) {
        val assignment = meetingPartnerRepository.findActiveByMeetingIdAndPartnerId(meetingId, partnerId, true)
        if (assignment.isPresent) {
            val meetingPartner = assignment.get()
            meetingPartner.active = false
            meetingPartnerRepository.save(meetingPartner)
        }
    }

    private fun toMeetingPartnerOutputDto(entity: MeetingPartnerEntity): MeetingPartnerOutputDto {
        return MeetingPartnerOutputDto(
            id = entity.id,
            meetingId = entity.meeting.id,
            meetingName = entity.meeting.name,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            partnerNumber = entity.partner.partnerNumber,
            partnerIdentificationNumber = entity.partner.partnerIdentificationNumber,
            active = entity.active,
            createdAt = entity.createdAt
        )
    }
}

