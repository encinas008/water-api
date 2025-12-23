package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.service.PartnerService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/partners")
class PartnerController(
    val partnerService: PartnerService,
) {

    @GetMapping
    fun getAll(): List<PartnerOutputDto> {
        return partnerService.findAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): PartnerOutputDto {
        return partnerService.findById(id)
    }

    @GetMapping("/{id}/debt-summary")
    fun getDebtSummary(@PathVariable id: UUID): PartnerDebtSummaryDto {
        return partnerService.getPartnerWithDebtSummary(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody input: PartnerInputDto): PartnerOutputDto {
        return partnerService.createPartner(input)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody input: PartnerUpdateDto): PartnerOutputDto {
        return partnerService.updatePartner(id, input)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        partnerService.deactivatePartner(id)
    }

    @PatchMapping("/{id}/connection-status")
    fun updateConnectionStatus(
        @PathVariable id: UUID,
        @RequestBody input: ConnectionStatusUpdateDto
    ): PartnerOutputDto {
        return partnerService.updateConnectionStatus(id, input)
    }

    @GetMapping("/search")
    fun search(@RequestParam q: String): List<PartnerOutputDto> {
        return partnerService.searchPartners(q)
    }
}
