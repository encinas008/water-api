package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.AssignPartnersToJobDto
import com.dreamsbo.posapi.dto.JobPartnerAssignmentDto
import com.dreamsbo.posapi.dto.JobPartnerOutputDto
import com.dreamsbo.posapi.dto.PartnerAssignmentInfoDto
import com.dreamsbo.posapi.persistence.entity.JobPartnerEntity
import com.dreamsbo.posapi.persistence.repository.JobPartnerRepository
import com.dreamsbo.posapi.persistence.repository.JobRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class JobPartnerService(
    private val jobPartnerRepository: JobPartnerRepository,
    private val jobRepository: JobRepository,
    private val partnerRepository: PartnerRepository,
) {

    fun getPartnersByJobId(jobId: UUID): List<JobPartnerOutputDto> {
        val jobPartners = jobPartnerRepository.findByJobIdAndActive(jobId, true)
        return jobPartners.map { toJobPartnerOutputDto(it) }
    }

    fun getJobsByPartnerId(partnerId: UUID): List<JobPartnerOutputDto> {
        val jobPartners = jobPartnerRepository.findByPartnerIdAndActive(partnerId, true)
        return jobPartners.map { toJobPartnerOutputDto(it) }
    }

    fun getJobWithPartnerAssignments(jobId: UUID): JobPartnerAssignmentDto {
        val jobEntity = jobRepository.findById(jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = $jobId")
        }

        val job = jobEntity.get()

        // Obtener todos los socios activos ordenados por partnerNumber ASC
        val allPartners = partnerRepository.findAllByActive(
            true,
            Sort.by(Sort.Direction.ASC, "partnerNumber")
        )

        // Obtener asignaciones existentes para este trabajo
        val existingAssignments = jobPartnerRepository.findByJobIdAndActive(jobId, true)
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

        return JobPartnerAssignmentDto(
            jobId = job.id,
            jobName = job.name,
            assignedPartners = partnerAssignments
        )
    }

    @Transactional
    fun assignPartnersToJob(jobId: UUID, input: AssignPartnersToJobDto): List<JobPartnerOutputDto> {
        val jobEntity = jobRepository.findById(jobId)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = $jobId")
        }

        val job = jobEntity.get()

        // Desactivar todas las asignaciones existentes
        val existingAssignments = jobPartnerRepository.findByJobIdAndActive(jobId, true)
        existingAssignments.forEach { assignment ->
            assignment.active = false
            jobPartnerRepository.save(assignment)
        }

        // Crear nuevas asignaciones para los socios seleccionados
        val newAssignments = mutableListOf<JobPartnerEntity>()

        for (partnerId in input.partnerIds) {
            val partnerEntity = partnerRepository.findById(partnerId)
            if (partnerEntity.isEmpty) {
                continue // Saltar si el socio no existe
            }

            val partner = partnerEntity.get()

            // Verificar si ya existe una asignación (aunque esté inactiva)
            val existingAssignment = jobPartnerRepository.findByJobIdAndPartnerId(jobId, partnerId)

            if (existingAssignment.isPresent) {
                // Reactivar la asignación existente
                val assignment = existingAssignment.get()
                assignment.active = true
                jobPartnerRepository.save(assignment)
                newAssignments.add(assignment)
            } else {
                // Crear nueva asignación
                val newAssignment = JobPartnerEntity(
                    job = job,
                    partner = partner
                )
                val saved = jobPartnerRepository.save(newAssignment)
                newAssignments.add(saved)
            }
        }

        return newAssignments.map { toJobPartnerOutputDto(it) }
    }

    @Transactional
    fun removePartnerFromJob(jobId: UUID, partnerId: UUID) {
        val assignment = jobPartnerRepository.findActiveByJobIdAndPartnerId(jobId, partnerId)
        if (assignment.isPresent) {
            val jobPartner = assignment.get()
            jobPartner.active = false
            jobPartnerRepository.save(jobPartner)
        }
    }

    private fun toJobPartnerOutputDto(entity: JobPartnerEntity): JobPartnerOutputDto {
        return JobPartnerOutputDto(
            id = entity.id,
            jobId = entity.job.id,
            jobName = entity.job.name,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            partnerIdentificationNumber = entity.partner.partnerIdentificationNumber,
            active = entity.active,
            createdAt = entity.createdAt
        )
    }
}

