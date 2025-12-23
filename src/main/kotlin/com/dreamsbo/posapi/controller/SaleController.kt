package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.SaleDetailOutputDto
import com.dreamsbo.posapi.dto.SaleInputDto
import com.dreamsbo.posapi.dto.SaleOutputDto
import com.dreamsbo.posapi.service.SaleService
import jakarta.websocket.server.PathParam
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*

@RestController
@RequestMapping("/sales")
class SaleController(
    val saleService: SaleService,
) {

    @PostMapping
    fun create(@RequestBody saleInputDto: SaleInputDto): SaleOutputDto {

        return saleService.create(saleInputDto)
    }

    @GetMapping("/{userId}")
    fun getAllByUser(
        @PathVariable("userId") userId: UUID,
        @PathParam("fromDate") fromDate: Long,
        @PathParam("toDate") toDate: Long
    ): List<SaleDetailOutputDto> {

        return saleService.getAllByUser(userId, fromDate, toDate)
    }
}
