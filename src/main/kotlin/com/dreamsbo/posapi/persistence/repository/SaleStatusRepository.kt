package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.SaleStatusEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SaleStatusRepository : JpaRepository<SaleStatusEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableSet<SaleStatusEntity>

    fun findByNameAndActive(name: String, active: Boolean): Optional<SaleStatusEntity>
}
