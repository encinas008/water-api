package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.AssignPartnersToJobDto
import com.dreamsbo.posapi.dto.JobPartnerAssignmentDto
import com.dreamsbo.posapi.dto.JobPartnerOutputDto
import com.dreamsbo.posapi.service.JobPartnerService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/jobs/{jobId}/partners")
class JobPartnerController(
    val jobPartnerService: JobPartnerService,
) {

    @GetMapping
    fun getPartnersByJob(@PathVariable jobId: UUID): List<JobPartnerOutputDto> {
        return jobPartnerService.getPartnersByJobId(jobId)
    }

    @GetMapping("/assignments")
    fun getJobWithPartnerAssignments(@PathVariable jobId: UUID): JobPartnerAssignmentDto {
        return jobPartnerService.getJobWithPartnerAssignments(jobId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun assignPartnersToJob(
        @PathVariable jobId: UUID,
        @RequestBody input: AssignPartnersToJobDto
    ): List<JobPartnerOutputDto> {
        return jobPartnerService.assignPartnersToJob(jobId, input)
    }

    @DeleteMapping("/{partnerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removePartnerFromJob(
        @PathVariable jobId: UUID,
        @PathVariable partnerId: UUID
    ) {
        jobPartnerService.removePartnerFromJob(jobId, partnerId)
    }
}
