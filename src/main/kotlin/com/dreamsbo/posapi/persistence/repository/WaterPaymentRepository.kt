package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.WaterPaymentEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface WaterPaymentRepository : JpaRepository<WaterPaymentEntity, UUID> {

    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean, sort: Sort): List<WaterPaymentEntity>

    fun findByWaterBillIdAndActive(waterBillId: UUID, active: Boolean): List<WaterPaymentEntity>

    fun findByPaymentDateBetweenAndActive(
        startDate: LocalDate,
        endDate: LocalDate,
        active: Boolean,
        sort: Sort
    ): List<WaterPaymentEntity>

    fun findByReceiptNumberAndActive(receiptNumber: String, active: Boolean): Optional<WaterPaymentEntity>

    @Query("SELECT p FROM WaterPaymentEntity p WHERE p.cashBalance.id = :cashBalanceId AND p.active = :active")
    fun findByCashBalanceId(cashBalanceId: UUID, active: Boolean = true): List<WaterPaymentEntity>

    @Query("SELECT SUM(p.amount) FROM WaterPaymentEntity p WHERE p.partner.id = :partnerId AND p.active = :active")
    fun sumPaymentsByPartnerId(partnerId: UUID, active: Boolean): Double?
}
