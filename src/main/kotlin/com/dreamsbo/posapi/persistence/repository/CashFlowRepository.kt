package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CashFlowEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
interface CashFlowRepository : JpaRepository<CashFlowEntity, UUID> {

    @Query("SELECT c FROM CashFlowEntity c WHERE c.cashBalance.box.user.id = :userId AND c.createdAt >= :fromDate AND c.createdAt < :toDate ORDER BY c.createdAt DESC")
    fun findCashFlowsByUserAndRangeOfDates(
        userId: UUID,
        fromDate: OffsetDateTime, toDate: OffsetDateTime
    ): List<CashFlowEntity>

    fun findByCashBalanceIdAndPaymentTypeName(cashBalanceId: UUID, paymentTypeName: String): List<CashFlowEntity>

    fun findByCashBalanceId(cashBalanceId: UUID): List<CashFlowEntity>
}
