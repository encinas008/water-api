package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.DetailedDebtorsReportDto
import com.dreamsbo.posapi.service.WaterReportService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/water-reports")
class WaterReportController(
    private val waterReportService: WaterReportService
) {

    @GetMapping("/debtors")
    fun getDetailedDebtorsReport(): DetailedDebtorsReportDto {
        return waterReportService.getDetailedDebtorsReport()
    }
}
