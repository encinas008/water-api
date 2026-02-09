package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.RoleInputDto
import com.dreamsbo.posapi.dto.RoleOutputDto
import com.dreamsbo.posapi.persistence.entity.RoleEntity
import com.dreamsbo.posapi.persistence.repository.RoleRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    fun findAll(): List<RoleOutputDto> {
        return roleRepository.findAll(Sort.by("name")).map {
            toOutputDto(it)
        }
    }

    fun findById(id: UUID): RoleOutputDto {
        val role = roleRepository.findById(id).orElseThrow { NotFoundEntityException("Rol no encontrado") }
        return toOutputDto(role)
    }

    @Transactional
    fun create(input: RoleInputDto): RoleOutputDto {
        val role = RoleEntity(
            code = input.code,
            name = input.name,
            description = input.description ?: ""
        )
        return toOutputDto(roleRepository.save(role))
    }

    @Transactional
    fun update(id: UUID, input: RoleInputDto): RoleOutputDto {
        val role = roleRepository.findById(id).orElseThrow { NotFoundEntityException("Rol no encontrado") }
        role.name = input.name
        role.description = input.description ?: ""
        role.code = input.code
        return toOutputDto(roleRepository.save(role))
    }

    @Transactional
    fun delete(id: UUID) {
        val role = roleRepository.findById(id).orElseThrow { NotFoundEntityException("Rol no encontrado") }
        role.active = false
        roleRepository.save(role)
    }

    private fun toOutputDto(it: RoleEntity): RoleOutputDto {
        return RoleOutputDto(
            id = it.id,
            name = it.name,
            description = it.description,
            code = it.code
        )
    }
}
