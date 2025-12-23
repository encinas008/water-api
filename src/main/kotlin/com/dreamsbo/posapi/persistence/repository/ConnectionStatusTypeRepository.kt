package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.ConnectionStatusTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConnectionStatusTypeRepository : JpaRepository<ConnectionStatusTypeEntity, UUID> {

    fun findByCodeAndActive(code: String, active: Boolean): Optional<ConnectionStatusTypeEntity>

    fun findByNameAndActive(name: String, active: Boolean): Optional<ConnectionStatusTypeEntity>
}
