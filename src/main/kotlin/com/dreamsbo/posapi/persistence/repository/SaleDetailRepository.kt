package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.SaleDetailEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SaleDetailRepository : JpaRepository<SaleDetailEntity, UUID> {

    fun findBySaleId(saleId: UUID): List<SaleDetailEntity>
}
