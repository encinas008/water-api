package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.JobInputDto
import com.dreamsbo.posapi.dto.JobOutputDto
import com.dreamsbo.posapi.dto.JobUpdateDto
import com.dreamsbo.posapi.persistence.entity.JobEntity
import com.dreamsbo.posapi.persistence.repository.JobRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class JobService(
    private val jobRepository: JobRepository,
) {

    fun findAll(): List<JobOutputDto> {
        val jobs = mutableListOf<JobOutputDto>()

        jobRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt")).forEach {
            jobs.add(toJobOutputDto(it))
        }

        return jobs
    }

    fun findById(id: UUID): JobOutputDto {
        val jobEntity = jobRepository.findById(id)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = $id")
        }
        return toJobOutputDto(jobEntity.get())
    }

    @Transactional
    fun createJob(input: JobInputDto): JobOutputDto {
        val job = JobEntity(
            name = input.name,
            startDate = input.startDate,
            description = input.description
        )

        val savedJob = jobRepository.save(job)
        return toJobOutputDto(savedJob)
    }

    @Transactional
    fun updateJob(id: UUID, input: JobUpdateDto): JobOutputDto {
        val jobEntity = jobRepository.findById(id)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = $id")
        }

        val job = jobEntity.get()

        input.name?.let { job.name = it }
        input.startDate?.let { job.startDate = it }
        input.description?.let { job.description = it }

        job.updatedAt = OffsetDateTime.now()

        val updatedJob = jobRepository.save(job)
        return toJobOutputDto(updatedJob)
    }

    @Transactional
    fun deleteJob(id: UUID) {
        val jobEntity = jobRepository.findById(id)
        if (jobEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el trabajo. JobId = $id")
        }

        val job = jobEntity.get()
        job.active = false
        job.updatedAt = OffsetDateTime.now()
        jobRepository.save(job)
    }

    private fun toJobOutputDto(entity: JobEntity): JobOutputDto {
        return JobOutputDto(
            id = entity.id,
            name = entity.name,
            startDate = entity.startDate,
            description = entity.description,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
