package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.GenderTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GenderTypeRepository : JpaRepository<GenderTypeEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableSet<GenderTypeEntity>
}
