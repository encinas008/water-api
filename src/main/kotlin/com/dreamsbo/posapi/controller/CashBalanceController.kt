package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.CashBalanceDetailsOutputDto
import com.dreamsbo.posapi.dto.CashBalanceInputDto
import com.dreamsbo.posapi.dto.CashBalanceOutputDto
import com.dreamsbo.posapi.dto.CloseCashBalanceInputDto
import com.dreamsbo.posapi.service.CashBalanceService
import jakarta.websocket.server.PathParam
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/cash-balances")
class CashBalanceController(
    val cashBalanceService: CashBalanceService,
) {

    @GetMapping("/users/{userId}")
    fun getAllCashBalanceByUser(
        @PathVariable("userId") userId: UUID, @PathParam("fromDate") fromDate: Long,
        @PathParam("toDate") toDate: Long
    ): List<CashBalanceOutputDto> {

        return cashBalanceService.findAllByUserId(userId, fromDate, toDate)
    }

    @GetMapping("/users/{userId}/last-open")
    fun getLastCashBalanceOpenByUser(
        @PathVariable("userId") userId: UUID
    ): List<CashBalanceOutputDto> {
        return cashBalanceService.findLastCashBalanceByUser(userId)
    }

    @GetMapping("/all-open")
    fun getAllOpenCashBalances(): List<CashBalanceOutputDto> {
        return cashBalanceService.findAllOpenCashBalances()
    }

    @PostMapping
    fun createCashBalance(@RequestBody cashBalanceInputDto: CashBalanceInputDto): CashBalanceOutputDto {

        return cashBalanceService.create(cashBalanceInputDto)
    }

    @PostMapping("/close")
    fun closeCashBalance(@RequestBody closeCashBalanceInputDto: CloseCashBalanceInputDto): Boolean {

        return cashBalanceService.close(closeCashBalanceInputDto)
    }

    @GetMapping("/{id}")
    fun getCashBalance(@PathVariable("id") cashBalanceId: UUID): CashBalanceDetailsOutputDto {

        return cashBalanceService.getDetails(cashBalanceId)
    }

    @GetMapping
    fun getAllCashBalances(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) userId: UUID?
    ): Page<CashBalanceOutputDto> {
        return cashBalanceService.findAllPaginated(page, size, search, userId)
    }
}
