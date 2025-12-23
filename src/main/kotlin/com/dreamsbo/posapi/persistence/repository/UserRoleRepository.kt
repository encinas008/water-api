package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.UserRoleEntity
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRoleRepository : JpaRepository<UserRoleEntity, UUID> {

    @Query("SELECT userRole FROM UserRoleEntity userRole WHERE userRole.role.name = :roleName")
    fun findAllUsersByRoleName(
        @Param("roleName") roleName: String,
    ): List<UserRoleEntity>
}
