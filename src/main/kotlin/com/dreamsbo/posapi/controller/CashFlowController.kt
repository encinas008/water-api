package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.CashFlowInputDto
import com.dreamsbo.posapi.dto.CashFlowOutputDto
import com.dreamsbo.posapi.service.CashFlowService
import jakarta.websocket.server.PathParam
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/cash-flows")
class CashFlowController(
    val cashFlowService: CashFlowService,
) {

    @GetMapping("/users/{userId}")
    fun getAllCashFlowsByUser(
        @PathVariable("userId") userId: UUID, @PathParam("fromDate") fromDate: Long,
        @PathParam("toDate") toDate: Long
    ): List<CashFlowOutputDto> {

        return cashFlowService.findAllByUserId(userId, fromDate, toDate)
    }

    @PostMapping
    fun createCashFlow(@RequestBody cashFlowInputDto: CashFlowInputDto): CashFlowOutputDto {

        return cashFlowService.create(cashFlowInputDto)
    }

    @GetMapping("/cash-balance/{cashBalanceId}/withdrawals")
    fun getWithdrawalsByCashBalance(@PathVariable("cashBalanceId") cashBalanceId: UUID): List<CashFlowOutputDto> {
        return cashFlowService.findWithdrawalsByCashBalanceId(cashBalanceId)
    }
}
