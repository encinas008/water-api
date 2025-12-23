package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.service.AttendanceService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/attendance")
class AttendanceController(
    val attendanceService: AttendanceService,
) {

    @GetMapping("/job/{jobId}")
    fun getAttendanceByJob(@PathVariable jobId: UUID): List<AttendanceOutputDto> {
        return attendanceService.getAttendanceByJob(jobId)
    }

    @GetMapping("/job/{jobId}/date/{date}")
    fun getAttendanceByJobAndDate(
        @PathVariable jobId: UUID,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): List<AttendanceOutputDto> {
        return attendanceService.getAttendanceByJobAndDate(jobId, date)
    }

    @GetMapping("/job/{jobId}/date-range")
    fun getAttendanceByJobAndDateRange(
        @PathVariable jobId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): List<AttendanceOutputDto> {
        return attendanceService.getAttendanceByJobAndDateRange(jobId, startDate, endDate)
    }

    @GetMapping("/job/{jobId}/grouped")
    fun getAttendanceGroupedByDate(
        @PathVariable jobId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): List<AttendanceByDateDto> {
        return attendanceService.getAttendanceGroupedByDate(jobId, startDate, endDate)
    }

    @GetMapping("/partner/{partnerId}")
    fun getAttendanceByPartner(@PathVariable partnerId: UUID): List<AttendanceOutputDto> {
        return attendanceService.getAttendanceByPartner(partnerId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAttendance(@RequestBody input: AttendanceInputDto): AttendanceOutputDto {
        return attendanceService.createAttendance(input)
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    fun bulkCreateAttendance(@RequestBody input: BulkAttendanceInputDto): List<AttendanceOutputDto> {
        return attendanceService.bulkCreateAttendance(input)
    }

    @PutMapping("/{id}")
    fun updateAttendance(
        @PathVariable id: UUID,
        @RequestBody input: AttendanceUpdateDto
    ): AttendanceOutputDto {
        return attendanceService.updateAttendance(id, input)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAttendance(@PathVariable id: UUID) {
        attendanceService.deleteAttendance(id)
    }
}

