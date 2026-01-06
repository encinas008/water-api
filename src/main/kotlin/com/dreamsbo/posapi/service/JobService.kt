package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.JobInputDto
import com.dreamsbo.posapi.dto.JobOutputDto
import com.dreamsbo.posapi.dto.JobUpdateDto
import com.dreamsbo.posapi.persistence.entity.JobEntity
import com.dreamsbo.posapi.persistence.repository.JobRepository
import com.dreamsbo.posapi.persistence.repository.JobAttendanceRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class JobService(
    private val jobRepository: JobRepository,
    private val jobAttendanceRepository: JobAttendanceRepository
) {

    fun findAll(): List<JobOutputDto> {
        val jobs = mutableListOf<JobOutputDto>()

        jobRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt")).forEach {
            jobs.add(toJobOutputDto(it))
        }

        return jobs
    }

    fun getScheduledDates(): List<String> {
        return jobRepository.findDistinctStartDatesByActive(true).map { it.toString() }
    }

    fun findAllPaginated(page: Int, size: Int, search: String?, date: java.time.LocalDate? = null): Page<JobOutputDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        val jobPage = when {
            !search.isNullOrBlank() && date != null -> {
                jobRepository.findAllByActiveAndSearchAndStartDate(true, search.trim(), date, pageable)
            }
            !search.isNullOrBlank() -> {
                jobRepository.findAllByActiveAndSearch(true, search.trim(), pageable)
            }
            date != null -> {
                jobRepository.findAllByActiveAndStartDate(true, date, pageable)
            }
            else -> {
                jobRepository.findAllByActive(true, pageable)
            }
        }
        
        return jobPage.map { toJobOutputDto(it) }
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
            description = input.description,
            fine = input.fine
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
        
        val newDate = input.startDate
        if (newDate != null && newDate != job.startDate) {
            job.startDate = newDate
            
            // Actualizar la fecha en todas las asistencias relacionadas
            val attendances = jobAttendanceRepository.findByJobIdAndActive(job.id, true)
            attendances.forEach { attendance ->
                attendance.attendanceDate = newDate
                attendance.updatedAt = OffsetDateTime.now()
            }
            jobAttendanceRepository.saveAll(attendances)
        }
        
        input.description?.let { job.description = it }
        input.fine?.let { job.fine = it }

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
            fine = entity.fine,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
