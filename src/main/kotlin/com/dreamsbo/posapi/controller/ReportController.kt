package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.TicketKitchenInputDto
import com.dreamsbo.posapi.service.ReportService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}
