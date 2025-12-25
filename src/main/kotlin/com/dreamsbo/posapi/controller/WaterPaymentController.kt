package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.MonthlyPendingFinesDto
import com.dreamsbo.posapi.dto.PaymentReceiptDto
import com.dreamsbo.posapi.dto.PaymentReceiptFullDto
import com.dreamsbo.posapi.dto.WaterPaymentInputDto
import com.dreamsbo.posapi.dto.WaterPaymentOutputDto
import com.dreamsbo.posapi.service.MonthlyPendingFinesService
import com.dreamsbo.posapi.service.WaterPaymentService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/water-payments")
class WaterPaymentController(
    private val waterPaymentService: WaterPaymentService,
    private val monthlyPendingFinesService: MonthlyPendingFinesService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun recordPayment(
        @RequestBody input: WaterPaymentInputDto
    ): WaterPaymentOutputDto {
        return waterPaymentService.recordPayment(input)
    }

    @GetMapping("/partner/{partnerId}")
    fun getPaymentHistory(@PathVariable partnerId: UUID): List<WaterPaymentOutputDto> {
        return waterPaymentService.getPaymentHistory(partnerId)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): WaterPaymentOutputDto {
        return waterPaymentService.getPaymentById(id)
    }

    @GetMapping("/receipt/{id}")
    fun getReceipt(@PathVariable id: UUID): PaymentReceiptDto {
        return waterPaymentService.generateReceipt(id)
    }

    @GetMapping("/receipt-full/{id}")
    fun getFullReceipt(
        @PathVariable id: UUID,
        @RequestParam(required = false, defaultValue = "NOTA DE PAGO") receiptType: String
    ): PaymentReceiptFullDto {
        return waterPaymentService.generateFullReceipt(id, receiptType)
    }

    @GetMapping("/pending-fines/partner/{partnerId}")
    fun getCurrentMonthPendingFines(@PathVariable partnerId: UUID): MonthlyPendingFinesDto {
        return monthlyPendingFinesService.getCurrentMonthPendingFines(partnerId)
    }

    @GetMapping("/pending-fines/partner/{partnerId}/month/{month}/year/{year}")
    fun getMonthlyPendingFines(
        @PathVariable partnerId: UUID,
        @PathVariable month: Int,
        @PathVariable year: Int
    ): MonthlyPendingFinesDto {
        return monthlyPendingFinesService.getMonthlyPendingFines(partnerId, month, year)
    }
}
