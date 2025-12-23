package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.AssignPartnersToMeetingDto
import com.dreamsbo.posapi.dto.MeetingPartnerAssignmentDto
import com.dreamsbo.posapi.dto.MeetingPartnerOutputDto
import com.dreamsbo.posapi.service.MeetingPartnerService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/meetings/{meetingId}/partners")
class MeetingPartnerController(
    private val meetingPartnerService: MeetingPartnerService
) {

    @GetMapping
    fun getPartnersByMeetingId(@PathVariable meetingId: UUID): List<MeetingPartnerOutputDto> {
        return meetingPartnerService.getPartnersByMeetingId(meetingId)
    }

    @GetMapping("/assignments")
    fun getMeetingWithPartnerAssignments(@PathVariable meetingId: UUID): MeetingPartnerAssignmentDto {
        return meetingPartnerService.getMeetingWithPartnerAssignments(meetingId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun assignPartnersToMeeting(@PathVariable meetingId: UUID, @RequestBody input: AssignPartnersToMeetingDto): List<MeetingPartnerOutputDto> {
        return meetingPartnerService.assignPartnersToMeeting(meetingId, input)
    }

    @DeleteMapping("/{partnerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removePartnerFromMeeting(@PathVariable meetingId: UUID, @PathVariable partnerId: UUID) {
        meetingPartnerService.removePartnerFromMeeting(meetingId, partnerId)
    }
}

