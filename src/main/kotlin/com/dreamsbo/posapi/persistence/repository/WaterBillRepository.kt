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
        AND (
            LOWER(b.partner.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(b.billNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(b.partner.waterConnectionNumber) LIKE LOWER(CONCAT('%', :search, '%'))
        )
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
        AND (
            LOWER(b.partner.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(b.billNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(b.partner.waterConnectionNumber) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findAllByActiveAndStatusAndSearch(
        @Param("active") active: Boolean,
        @Param("statusCode") statusCode: String,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<WaterBillEntity>
}
