package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.RoleOutputDto
import com.dreamsbo.posapi.service.RoleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/roles")
class RoleController(
    private val roleService: RoleService
) {

    @GetMapping
    fun getAll(): List<RoleOutputDto> {
        return roleService.findAll()
    }
}
