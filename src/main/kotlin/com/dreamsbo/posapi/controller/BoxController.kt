package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.BoxOutputDto
import com.dreamsbo.posapi.service.BoxService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/boxes")
class BoxController(
    val boxService: BoxService,
) {

    @GetMapping
    fun getAll(): List<BoxOutputDto> {

        return boxService.findAll()
    }
}
