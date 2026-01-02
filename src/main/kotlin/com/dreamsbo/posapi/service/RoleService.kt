package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.RoleOutputDto
import com.dreamsbo.posapi.persistence.repository.RoleRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    fun findAll(): List<RoleOutputDto> {
        return roleRepository.findAll(Sort.by("name")).map {
            RoleOutputDto(
                id = it.id,
                name = it.name,
                description = it.description,
                code = it.code
            )
        }
    }
}
