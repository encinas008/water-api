package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.ProfileEntity
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository : JpaRepository<ProfileEntity, UUID> {

    fun findByDni(dni: String): Optional<ProfileEntity>
}
