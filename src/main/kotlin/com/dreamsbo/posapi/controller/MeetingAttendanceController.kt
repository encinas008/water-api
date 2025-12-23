package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.service.MeetingAttendanceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/meetings/{meetingId}/attendance")
class MeetingAttendanceController(
    private val meetingAttendanceService: MeetingAttendanceService
) {

    @GetMapping
    fun getAttendanceByMeeting(@PathVariable meetingId: UUID): List<MeetingAttendanceOutputDto> {
        return meetingAttendanceService.getAttendanceByMeeting(meetingId)
    }

    @GetMapping("/date/{date}")
    fun getAttendanceByMeetingAndDate(
        @PathVariable meetingId: UUID,
        @PathVariable date: LocalDate
    ): List<MeetingAttendanceOutputDto> {
        return meetingAttendanceService.getAttendanceByMeetingAndDate(meetingId, date)
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    fun bulkCreateAttendance(
        @PathVariable meetingId: UUID,
        @RequestBody input: BulkMeetingAttendanceInputDto
    ): List<MeetingAttendanceOutputDto> {
        return meetingAttendanceService.bulkCreateAttendance(input)
    }

    @PutMapping("/{id}")
    fun updateAttendance(
        @PathVariable meetingId: UUID,
        @PathVariable id: UUID,
        @RequestBody input: MeetingAttendanceUpdateDto
    ): MeetingAttendanceOutputDto {
        return meetingAttendanceService.updateAttendance(id, input)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAttendance(@PathVariable meetingId: UUID, @PathVariable id: UUID) {
        meetingAttendanceService.deleteAttendance(id)
    }
}

