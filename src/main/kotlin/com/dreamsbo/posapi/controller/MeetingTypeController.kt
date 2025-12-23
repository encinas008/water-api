package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.MeetingTypeOutputDto
import com.dreamsbo.posapi.service.MeetingTypeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/meeting-types")
class MeetingTypeController(
    private val meetingTypeService: MeetingTypeService
) {
    @GetMapping
    fun getAll(): List<MeetingTypeOutputDto> {
        return meetingTypeService.findAll()
    }
}

