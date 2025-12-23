package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.SaleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
interface SaleRepository : JpaRepository<SaleEntity, UUID> {

    fun findByCashBalanceId(cashBalanceId: UUID): List<SaleEntity>

    fun findByCashBalanceIdAndPaymentTypeName(cashBalanceId: UUID, paymentTypeName: String): List<SaleEntity>

    @Query("SELECT s FROM SaleEntity s WHERE s.user.id = :userId AND s.createdAt >= :fromDate AND s.createdAt < :toDate ORDER BY s.createdAt DESC")
    fun findSalesByUserAndRangeOfDates(
        userId: UUID,
        fromDate: OffsetDateTime, toDate: OffsetDateTime
    ): List<SaleEntity>
}
