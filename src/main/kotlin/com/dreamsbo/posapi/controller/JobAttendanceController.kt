package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.service.JobAttendanceService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/jobs/{jobId}/attendance")
class JobAttendanceController(
    val jobAttendanceService: JobAttendanceService,
) {

    @GetMapping
    fun getAttendanceByJob(@PathVariable jobId: UUID): List<JobAttendanceOutputDto> {
        return jobAttendanceService.getAttendanceByJob(jobId)
    }

    @GetMapping("/date/{date}")
    fun getAttendanceByJobAndDate(
        @PathVariable jobId: UUID,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): List<JobAttendanceOutputDto> {
        return jobAttendanceService.getAttendanceByJobAndDate(jobId, date)
    }

    @GetMapping("/date-range")
    fun getAttendanceByJobAndDateRange(
        @PathVariable jobId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): List<JobAttendanceOutputDto> {
        return jobAttendanceService.getAttendanceByJobAndDateRange(jobId, startDate, endDate)
    }

    @GetMapping("/grouped")
    fun getAttendanceGroupedByDate(
        @PathVariable jobId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): List<JobAttendanceByDateDto> {
        return jobAttendanceService.getAttendanceGroupedByDate(jobId, startDate, endDate)
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    fun bulkCreateAttendance(
        @PathVariable jobId: UUID,
        @RequestBody input: BulkJobAttendanceInputDto
    ): List<JobAttendanceOutputDto> {
        // Asegurar que el jobId del path coincida con el del body
        val bulkInput = BulkJobAttendanceInputDto(
            jobId = jobId,
            attendanceDate = input.attendanceDate,
            attendances = input.attendances
        )
        return jobAttendanceService.bulkCreateAttendance(bulkInput)
    }

    @PutMapping("/{id}")
    fun updateAttendance(
        @PathVariable jobId: UUID,
        @PathVariable id: UUID,
        @RequestBody input: JobAttendanceUpdateDto
    ): JobAttendanceOutputDto {
        return jobAttendanceService.updateAttendance(id, input)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAttendance(@PathVariable jobId: UUID, @PathVariable id: UUID) {
        jobAttendanceService.deleteAttendance(id)
    }

    // Endpoints para reemplazar funcionalidad de JobPartnerController
    @GetMapping("/assignments")
    fun getJobWithPartnerAssignments(@PathVariable jobId: UUID): JobPartnerAssignmentDto {
        return jobAttendanceService.getJobWithPartnerAssignments(jobId)
    }

    @PostMapping("/assign-partners")
    @ResponseStatus(HttpStatus.OK)
    fun assignPartnersToJob(
        @PathVariable jobId: UUID,
        @RequestBody input: AssignPartnersToJobDto
    ): List<JobAttendanceOutputDto> {
        return jobAttendanceService.assignPartnersToJob(jobId, input)
    }

    @DeleteMapping("/partner/{partnerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removePartnerFromJob(@PathVariable jobId: UUID, @PathVariable partnerId: UUID) {
        jobAttendanceService.removePartnerFromJob(jobId, partnerId)
    }
}

// Controller adicional para endpoints que no requieren jobId
@RestController
@RequestMapping("/attendance")
class AttendanceGeneralController(
    val jobAttendanceService: JobAttendanceService,
) {
    @GetMapping("/partner/{partnerId}")
    fun getAttendanceByPartner(@PathVariable partnerId: UUID): List<JobAttendanceOutputDto> {
        return jobAttendanceService.getAttendanceByPartner(partnerId)
    }
}

