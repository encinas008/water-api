package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.WaterPaymentDetailEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WaterPaymentDetailRepository : JpaRepository<WaterPaymentDetailEntity, UUID> {
    
    fun findByWaterPaymentIdAndActive(paymentId: UUID, active: Boolean): List<WaterPaymentDetailEntity>
    
    fun findByWaterPaymentId(paymentId: UUID): List<WaterPaymentDetailEntity>
}



