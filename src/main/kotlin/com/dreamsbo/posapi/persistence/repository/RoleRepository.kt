package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.Optional

@Repository
interface RoleRepository : JpaRepository<RoleEntity, UUID> {

    fun findByCode(code: String): Optional<RoleEntity>

    fun findByNameAndActive(name: String, active: Boolean): Optional<RoleEntity>
}
