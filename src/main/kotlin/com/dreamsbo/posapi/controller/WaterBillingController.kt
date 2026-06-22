package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.AddBillConceptDto
import com.dreamsbo.posapi.dto.WaterBillDetailDto
import com.dreamsbo.posapi.dto.WaterBillGenerationDto
import com.dreamsbo.posapi.dto.WaterBillInputDto
import com.dreamsbo.posapi.dto.WaterBillOutputDto
import com.dreamsbo.posapi.dto.WaterBillSummaryDto
import com.dreamsbo.posapi.service.WaterBillingService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/water-bills")
class WaterBillingController(
    private val waterBillingService: WaterBillingService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBill(@RequestBody input: WaterBillInputDto): WaterBillOutputDto {
        return waterBillingService.createBill(input)
    }

    @GetMapping("/generate-monthly/preview")
    fun previewMonthlyBills(@RequestParam year: Int, @RequestParam month: Int): com.dreamsbo.posapi.dto.WaterBillGenerationPreviewDto {
        return waterBillingService.previewMonthlyBills(year, month)
    }

    @PostMapping("/generate-monthly")
    @ResponseStatus(HttpStatus.CREATED)
    fun generateMonthlyBills(@RequestBody input: WaterBillGenerationDto): List<WaterBillOutputDto> {
        return waterBillingService.generateMonthlyBills(input)
    }

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) statusCode: String?
    ): Page<WaterBillOutputDto> {
        return waterBillingService.findAllPaginated(page, size, search, statusCode)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): WaterBillOutputDto {
        return waterBillingService.getBillDetails(id)
    }

    @GetMapping("/{id}/detail")
    fun getBillDetailWithPayments(@PathVariable id: UUID): WaterBillDetailDto {
        return waterBillingService.getBillDetailWithPayments(id)
    }

    @GetMapping("/partner/{partnerId}")
    fun getBillsByPartner(@PathVariable partnerId: UUID): List<WaterBillOutputDto> {
        return waterBillingService.getBillsByPartner(partnerId)
    }

    @GetMapping("/pending")
    fun getPendingBills(): List<WaterBillOutputDto> {
        return waterBillingService.getPendingBills()
    }

    @GetMapping("/overdue")
    fun getOverdueBills(): List<WaterBillOutputDto> {
        return waterBillingService.getOverdueBills()
    }

    @GetMapping("/summary")
    fun getBillStats(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) statusCode: String?
    ): com.dreamsbo.posapi.dto.WaterBillStatsDto {
        return waterBillingService.getBillStats(search, statusCode)
    }

    @PostMapping("/{id}/concepts")
    @ResponseStatus(HttpStatus.CREATED)
    fun addConcept(
        @PathVariable id: UUID,
        @RequestBody input: AddBillConceptDto
    ): WaterBillOutputDto {
        return waterBillingService.addConceptToBill(id, input)
    }

    @DeleteMapping("/{id}/concepts/{conceptId}")
    fun deleteConcept(
        @PathVariable id: UUID,
        @PathVariable conceptId: UUID
    ): WaterBillOutputDto {
        return waterBillingService.removeConceptFromBill(id, conceptId)
    }

    @PostMapping("/{id}/cancel")
    fun cancelBill(
        @PathVariable id: UUID,
        @RequestParam userId: UUID
    ): WaterBillOutputDto {
        return waterBillingService.cancelBill(id, userId)
    }

    @PostMapping("/{id}/waive")
    fun waiveBill(
        @PathVariable id: UUID,
        @RequestParam userId: UUID
    ): WaterBillOutputDto {
        return waterBillingService.waiveBill(id, userId)
    }
}
