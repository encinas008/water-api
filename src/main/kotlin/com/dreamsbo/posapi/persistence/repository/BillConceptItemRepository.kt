package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.BillConceptItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BillConceptItemRepository : JpaRepository<BillConceptItemEntity, UUID> {
    fun findByWaterBillIdAndActive(waterBillId: UUID, active: Boolean): List<BillConceptItemEntity>
}






