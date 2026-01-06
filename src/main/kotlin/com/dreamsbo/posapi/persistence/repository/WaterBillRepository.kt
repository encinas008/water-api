package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.WaterBillEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface WaterBillRepository : JpaRepository<WaterBillEntity, UUID> {

    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean, sort: Sort): List<WaterBillEntity>
    
    @Query("SELECT COUNT(b) FROM WaterBillEntity b WHERE b.partner.id = :partnerId AND b.status.code IN ('PENDING', 'OVERDUE', 'PARTIAL_PAID') AND b.active = true")
    fun countUnpaidBillsByPartnerId(@Param("partnerId") partnerId: UUID): Long

    fun findByStatusCodeAndActive(statusCode: String, active: Boolean): List<WaterBillEntity>

    @Query("SELECT b FROM WaterBillEntity b WHERE b.status.code IN :statusCodes AND b.active = :active")
    fun findByStatusCodesAndActive(statusCodes: List<String>, active: Boolean): List<WaterBillEntity>

    @Query("SELECT b FROM WaterBillEntity b WHERE b.dueDate < :currentDate AND b.status.code NOT IN ('PAID', 'CANCELLED') AND b.active = :active")
    fun findOverdueBills(currentDate: LocalDate, active: Boolean): List<WaterBillEntity>

    fun findByBillingPeriodStartBetweenAndActive(
        startDate: LocalDate,
        endDate: LocalDate,
        active: Boolean
    ): List<WaterBillEntity>

    fun findByBillNumberAndActive(billNumber: String, active: Boolean): Optional<WaterBillEntity>
    
    fun findAllByActive(active: Boolean, pageable: Pageable): Page<WaterBillEntity>
    
    @Query("""
        SELECT b FROM WaterBillEntity b 
        WHERE b.active = :active 
        AND CAST(b.partner.partnerNumber AS string) LIKE CONCAT('%', :search, '%')
    """)
    fun findAllByActiveAndSearch(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<WaterBillEntity>
    
    @Query("""
        SELECT b FROM WaterBillEntity b 
        WHERE b.active = :active 
        AND b.status.code = :statusCode
    """)
    fun findAllByActiveAndStatus(
        @Param("active") active: Boolean,
        @Param("statusCode") statusCode: String,
        pageable: Pageable
    ): Page<WaterBillEntity>
    
    @Query("""
        SELECT b FROM WaterBillEntity b 
        WHERE b.active = :active 
        AND b.status.code = :statusCode
        AND CAST(b.partner.partnerNumber AS string) LIKE CONCAT('%', :search, '%')
    """)
    fun findAllByActiveAndStatusAndSearch(
        @Param("active") active: Boolean,
        @Param("statusCode") statusCode: String,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<WaterBillEntity>
    @Query("""
        SELECT 
            COUNT(b),
            SUM(CASE WHEN b.status.code = 'PENDING' THEN 1 ELSE 0 END),
            SUM(CASE WHEN b.status.code IN ('OVERDUE', 'CANCELLED') AND b.dueDate < :currentDate AND b.status.code != 'PAID' THEN 1 ELSE 0 END),
            SUM(CASE WHEN b.status.code = 'PAID' THEN 1 ELSE 0 END),
            SUM(CASE WHEN b.status.code = 'PENDING' THEN b.remainingBalance ELSE 0 END),
            SUM(CASE WHEN b.status.code IN ('OVERDUE') THEN b.remainingBalance ELSE 0 END),
            SUM(CASE WHEN b.status.code = 'PAID' THEN b.totalAmount ELSE 0 END)
        FROM WaterBillEntity b 
        WHERE b.active = :active
    """)
    fun getBillStats(
        @Param("currentDate") currentDate: LocalDate,
        @Param("active") active: Boolean
    ): List<Array<Any>>
}
