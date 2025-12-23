package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.WaterBillGenerationDto
import com.dreamsbo.posapi.dto.WaterBillInputDto
import com.dreamsbo.posapi.dto.WaterBillOutputDto
import com.dreamsbo.posapi.dto.WaterBillSummaryDto
import com.dreamsbo.posapi.service.WaterBillingService
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

    @PostMapping("/generate-monthly")
    @ResponseStatus(HttpStatus.CREATED)
    fun generateMonthlyBills(@RequestBody input: WaterBillGenerationDto): List<WaterBillOutputDto> {
        return waterBillingService.generateMonthlyBills(input)
    }

    @GetMapping
    fun getAll(): List<WaterBillOutputDto> {
        var data = waterBillingService.getAllBills()
        return data
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): WaterBillOutputDto {
        return waterBillingService.getBillDetails(id)
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
}
