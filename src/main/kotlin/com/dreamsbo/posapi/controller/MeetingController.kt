package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.MeetingInputDto
import com.dreamsbo.posapi.dto.MeetingOutputDto
import com.dreamsbo.posapi.dto.MeetingUpdateDto
import com.dreamsbo.posapi.service.MeetingService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/meetings")
class MeetingController(
    val meetingService: MeetingService
) {

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) date: java.time.LocalDate?
    ): Page<MeetingOutputDto> {
        return meetingService.findAllPaginated(page, size, search, date)
    }

    @GetMapping("/scheduled-dates")
    fun getScheduledDates(): List<String> {
        return meetingService.getScheduledDates()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): MeetingOutputDto {
        return meetingService.findById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody input: MeetingInputDto): MeetingOutputDto {
        return meetingService.createMeeting(input)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody input: MeetingUpdateDto): MeetingOutputDto {
        return meetingService.updateMeeting(id, input)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        meetingService.deleteMeeting(id)
    }
}



