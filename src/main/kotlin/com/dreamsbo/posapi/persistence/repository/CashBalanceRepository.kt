package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CashBalanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
interface CashBalanceRepository : JpaRepository<CashBalanceEntity, UUID> {

    @Query("SELECT c FROM CashBalanceEntity c WHERE c.box.user.id = :userId AND c.createdAt >= :fromDate AND c.createdAt < :toDate ORDER BY c.createdAt DESC")
    fun findCashBalancesByUserAndRangeOfDates(
        userId: UUID,
        fromDate: OffsetDateTime, toDate: OffsetDateTime
    ): List<CashBalanceEntity>

    @Query("SELECT c FROM CashBalanceEntity c WHERE c.box.user.id = :userId AND c.openTime IS NOT NULL AND c.closeTime is NULL AND c.active = true")
    fun findLastCashBalanceOpenForUser(userId: UUID): List<CashBalanceEntity>

    @Query("select c from CashBalanceEntity c where c.box.id = :boxId AND c.openTime is not null AND c.closeTime is NULL AND c.active = true")
    fun findOpenBoxForUser(boxId: UUID): Optional<CashBalanceEntity>
}
