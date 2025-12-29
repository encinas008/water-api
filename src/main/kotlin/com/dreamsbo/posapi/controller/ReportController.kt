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
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/reports")
class ReportController(val reportService: ReportService) {

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
}
