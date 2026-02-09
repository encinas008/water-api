package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.RoleInputDto
import com.dreamsbo.posapi.dto.RoleOutputDto
import com.dreamsbo.posapi.service.RoleService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/roles")
class RoleController(
    private val roleService: RoleService
) {

    @GetMapping
    fun getAll(): List<RoleOutputDto> {
        return roleService.findAll()
    }
    
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): RoleOutputDto {
        return roleService.findById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody input: RoleInputDto): RoleOutputDto {
        return roleService.create(input)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody input: RoleInputDto): RoleOutputDto {
        return roleService.update(id, input)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        roleService.delete(id)
    }
}
