package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.WaterBillEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
}
