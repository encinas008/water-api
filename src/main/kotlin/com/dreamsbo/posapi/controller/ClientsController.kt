package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.CategoryOutputDto
import com.dreamsbo.posapi.dto.ClientOutputDto
import com.dreamsbo.posapi.service.CategoryService
import com.dreamsbo.posapi.service.ClientsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clients")
class ClientsController(
    val clientService: ClientsService,
) {

    @GetMapping
    fun getAll(): List<ClientOutputDto> {

        return clientService.findAll()
    }
}
