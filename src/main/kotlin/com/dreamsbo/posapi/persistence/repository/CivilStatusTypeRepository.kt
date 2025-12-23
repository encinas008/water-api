package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CivilStatusTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CivilStatusTypeRepository : JpaRepository<CivilStatusTypeEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableSet<CivilStatusTypeEntity>
}
