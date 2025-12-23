package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.UserEntity
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun findByUsernameAndPassword(username: String, password: String): Optional<UserEntity>

    fun findByUsername(username: String?): Optional<UserEntity>
}
