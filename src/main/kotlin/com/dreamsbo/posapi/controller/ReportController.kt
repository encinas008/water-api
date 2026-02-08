package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.TicketKitchenInputDto
import com.dreamsbo.posapi.service.ReportService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID
import com.dreamsbo.posapi.dto.DailyMovementReportDto
import com.dreamsbo.posapi.dto.DebtReportDto
import com.dreamsbo.posapi.dto.MonthlyReadingsReportDto
import com.dreamsbo.posapi.dto.PartnerStatusReportDto
import com.dreamsbo.posapi.dto.MissingReadingItemDto
import com.dreamsbo.posapi.dto.DashboardStatsDto
import com.dreamsbo.posapi.dto.ExcessConsumptionReportDto
import java.math.BigDecimal

@RestController
@RequestMapping("/reports")
class ReportController(val reportService: ReportService) {

    @GetMapping("/dashboard-stats")
    fun getDashboardStats(@RequestParam(required = false) year: Int?): DashboardStatsDto {
        return reportService.getDashboardStats(year)
    }

    @GetMapping("/partner-consumption/{partnerId}")
    fun getPartnerConsumptionStats(
        @PathVariable partnerId: UUID,
        @RequestParam(required = false) year: Int?
    ): com.dreamsbo.posapi.dto.PartnerConsumptionStatsDto {
        return reportService.getPartnerConsumptionStats(partnerId, year)
    }

    @GetMapping("/movements")
    fun getMovementReport(
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): DailyMovementReportDto {
        return reportService.getMovementReport(
            LocalDate.parse(startDate),
            LocalDate.parse(endDate)
        )
    }

    @GetMapping("/readings")
    fun getMonthlyReadingsReport(
        @RequestParam year: Int,
        @RequestParam month: Int
    ): MonthlyReadingsReportDto {
        return reportService.getMonthlyReadingsReport(year, month)
    }

    @GetMapping("/missing-readings")
    fun getMissingReadingsReport(
        @RequestParam year: Int,
        @RequestParam month: Int
    ): List<MissingReadingItemDto> {
        return reportService.getMissingReadingsReport(year, month)
    }

    @GetMapping("/partners-status")
    fun getPartnerStatusReport(): PartnerStatusReportDto {
        return reportService.getPartnerStatusReport()
    }
    
    @GetMapping("/excess-consumption")
    fun getExcessConsumptionReport(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam(required = false) threshold: BigDecimal?
    ): ExcessConsumptionReportDto {
        return reportService.getExcessConsumptionReport(year, month, threshold)
    }

    @GetMapping("/cutoff-candidates")
    fun getCutoffCandidatesReport(): List<DebtReportDto> {
        return reportService.getCutoffCandidatesReport()
    }

    @PostMapping("/kitchen")
    fun getTicketKitchenReport(@RequestBody ticketKitchenInputDto: TicketKitchenInputDto): ResponseEntity<ByteArray> {

        val header = HttpHeaders()

        header.contentType = MediaType.APPLICATION_PDF
        header.setContentDispositionFormData("ticketCocina", "Comanda Cocina.pdf")

        val ticket = reportService.generateTicketKitchen(ticketKitchenInputDto)

        return ResponseEntity.ok().headers(header).body(ticket)
    }

    @PostMapping("/client")
    fun getTicketClientReport(@RequestBody ticketKitchenInputDto: TicketKitchenInputDto): ResponseEntity<ByteArray> {

        val header = HttpHeaders()

        header.contentType = MediaType.APPLICATION_PDF
        header.setContentDispositionFormData("ticketCliente", "Vente Cliente.pdf")

        val ticket = reportService.generateTicketClient(ticketKitchenInputDto)

        return ResponseEntity.ok().headers(header).body(ticket)
    }

    @GetMapping("/{id}/receipt-pdf")
    fun downloadReceiptPdf(@PathVariable id: UUID): ResponseEntity<ByteArray> {

        val pdfBytes = reportService.generateWaterPaymentReceiptPdf(id)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("inline", "factura_$id.pdf")
        headers.cacheControl = "must-revalidate, post-check=0, pre-check=0"

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(pdfBytes)
    }

    @GetMapping("/cash-flows/{id}/receipt-pdf")
    fun downloadCashFlowReceiptPdf(@PathVariable id: UUID): ResponseEntity<ByteArray> {

        val pdfBytes = reportService.generateCashFlowReceiptPdf(id)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("inline", "comprobante_$id.pdf")
        headers.cacheControl = "must-revalidate, post-check=0, pre-check=0"

        return ResponseEntity
            .ok()
            .headers(headers)
            .body(pdfBytes)
    }
}
