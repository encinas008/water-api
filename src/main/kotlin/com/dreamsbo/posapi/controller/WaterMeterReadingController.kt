package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.WaterMeterReadingInputDto
import com.dreamsbo.posapi.dto.WaterMeterReadingOutputDto
import com.dreamsbo.posapi.dto.WaterMeterReadingUpdateDto
import com.dreamsbo.posapi.service.WaterMeterReadingService
import org.springframework.data.domain.Page
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/water-readings")
class WaterMeterReadingController(
    private val waterMeterReadingService: WaterMeterReadingService,
) {

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): Page<WaterMeterReadingOutputDto> {
        return waterMeterReadingService.findAllPaginated(page, size, search)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun recordReading(
        @RequestBody input: WaterMeterReadingInputDto
    ): WaterMeterReadingOutputDto {
        return waterMeterReadingService.recordReading(input)
    }

    @GetMapping("/partner/{partnerId}")
    fun getReadingHistory(@PathVariable partnerId: UUID): List<WaterMeterReadingOutputDto> {
        return waterMeterReadingService.getReadingHistory(partnerId)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): WaterMeterReadingOutputDto {
        return waterMeterReadingService.getReadingById(id)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody input: WaterMeterReadingUpdateDto
    ): WaterMeterReadingOutputDto {
        return waterMeterReadingService.updateReading(id, input)
    }

    @GetMapping("/period")
    fun getByPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): List<WaterMeterReadingOutputDto> {
        return waterMeterReadingService.getReadingsByPeriod(startDate, endDate)
    }
}
