package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.service.JobService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/jobs")
class JobController(
    val jobService: JobService,
) {

    @GetMapping
    fun getAll(): List<JobOutputDto> {
        return jobService.findAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): JobOutputDto {
        return jobService.findById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody input: JobInputDto): JobOutputDto {
        return jobService.createJob(input)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody input: JobUpdateDto): JobOutputDto {
        return jobService.updateJob(id, input)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        jobService.deleteJob(id)
    }
}

