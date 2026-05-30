package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CashBalanceEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
interface CashBalanceRepository : JpaRepository<CashBalanceEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CashBalanceEntity c WHERE c.id = :id")
    fun findByIdLocked(id: UUID): Optional<CashBalanceEntity>

    @Query("SELECT c FROM CashBalanceEntity c WHERE c.box.user.id = :userId AND c.createdAt >= :fromDate AND c.createdAt < :toDate ORDER BY c.createdAt DESC")
    fun findCashBalancesByUserAndRangeOfDates(
        userId: UUID,
        fromDate: OffsetDateTime, toDate: OffsetDateTime
    ): List<CashBalanceEntity>

    @Query("SELECT c FROM CashBalanceEntity c WHERE c.box.user.id = :userId AND c.openTime IS NOT NULL AND c.closeTime is NULL AND c.active = true")
    fun findLastCashBalanceOpenForUser(userId: UUID): List<CashBalanceEntity>

    @Query("select c from CashBalanceEntity c where c.box.id = :boxId AND c.openTime is not null AND c.closeTime is NULL AND c.active = true")
    fun findOpenBoxForUser(boxId: UUID): Optional<CashBalanceEntity>

    @Query("SELECT c FROM CashBalanceEntity c WHERE c.openTime IS NOT NULL AND c.closeTime IS NULL AND c.active = true ORDER BY c.openTime DESC")
    fun findAllOpenCashBalances(): List<CashBalanceEntity>
    
    fun findAllByActive(active: Boolean, pageable: Pageable): Page<CashBalanceEntity>
    
    fun findAllByActiveAndBoxUserId(active: Boolean, userId: UUID, pageable: Pageable): Page<CashBalanceEntity>
    
    @Query("""
        SELECT c FROM CashBalanceEntity c 
        WHERE c.active = :active 
        AND (
            LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(CONCAT(c.box.user.profile.name, ' ', c.box.user.profile.lastname)) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY c.openTime DESC
    """)
    fun findAllByActiveAndSearch(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<CashBalanceEntity>

    @Query("""
        SELECT c FROM CashBalanceEntity c 
        WHERE c.active = :active 
        AND c.box.user.id = :userId
        AND (
            LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(CONCAT(c.box.user.profile.name, ' ', c.box.user.profile.lastname)) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY c.openTime DESC
    """)
    fun findAllByActiveAndSearchAndBoxUserId(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        @Param("userId") userId: UUID,
        pageable: Pageable
    ): Page<CashBalanceEntity>

    fun findByOpenTimeBetweenAndActive(start: OffsetDateTime, end: OffsetDateTime, active: Boolean): List<CashBalanceEntity>
}
