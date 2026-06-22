package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.WaterMeterReadingEntity
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
interface WaterMeterReadingRepository : JpaRepository<WaterMeterReadingEntity, UUID> {

    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean, sort: Sort): List<WaterMeterReadingEntity>

    fun countByActive(active: Boolean): Long

    @Query("SELECT r FROM WaterMeterReadingEntity r WHERE r.partner.id = :partnerId AND r.active = :active ORDER BY r.readingDate DESC LIMIT 1")
    fun findLatestByPartnerId(partnerId: UUID, active: Boolean): Optional<WaterMeterReadingEntity>

    @Query("SELECT r FROM WaterMeterReadingEntity r WHERE r.partner.id = :partnerId AND r.active = :active AND r.readingDate < :beforeDate ORDER BY r.readingDate DESC LIMIT 1")
    fun findLatestByPartnerIdBeforeDate(
        @Param("partnerId") partnerId: UUID,
        @Param("active") active: Boolean,
        @Param("beforeDate") beforeDate: LocalDate
    ): Optional<WaterMeterReadingEntity>

    @Query("SELECT r FROM WaterMeterReadingEntity r WHERE r.partner.id = :partnerId AND r.active = :active AND r.currentReading > 0 ORDER BY r.readingDate DESC LIMIT 1")
    fun findLatestNonZeroByPartnerId(partnerId: UUID, active: Boolean): Optional<WaterMeterReadingEntity>

    @Query("SELECT r FROM WaterMeterReadingEntity r WHERE r.partner.id = :partnerId AND r.active = :active AND r.currentReading > 0 AND r.readingDate < :beforeDate ORDER BY r.readingDate DESC LIMIT 1")
    fun findLatestNonZeroByPartnerIdBeforeDate(
        @Param("partnerId") partnerId: UUID,
        @Param("active") active: Boolean,
        @Param("beforeDate") beforeDate: LocalDate
    ): Optional<WaterMeterReadingEntity>

    fun findByReadingDateBetweenAndActive(
        startDate: LocalDate,
        endDate: LocalDate,
        active: Boolean
    ): List<WaterMeterReadingEntity>

    @Query("SELECT r FROM WaterMeterReadingEntity r WHERE r.partner.id = :partnerId AND YEAR(r.readingDate) = :year AND MONTH(r.readingDate) = :month AND r.active = :active")
    fun findByPartnerIdAndYearAndMonth(partnerId: UUID, year: Int, month: Int, active: Boolean): List<WaterMeterReadingEntity>
    
    fun findAllByActive(active: Boolean, pageable: Pageable): Page<WaterMeterReadingEntity>
    
    @Query("""
        SELECT r FROM WaterMeterReadingEntity r 
        WHERE r.active = :active 
        AND CAST(r.partner.partnerNumber AS string) = :search
    """)
    fun findAllByActiveAndSearch(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<WaterMeterReadingEntity>
}
