package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.WaterMeterReadingEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface WaterMeterReadingRepository : JpaRepository<WaterMeterReadingEntity, UUID> {

    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean, sort: Sort): List<WaterMeterReadingEntity>

    @Query("SELECT r FROM WaterMeterReadingEntity r WHERE r.partner.id = :partnerId AND r.active = :active ORDER BY r.readingDate DESC LIMIT 1")
    fun findLatestByPartnerId(partnerId: UUID, active: Boolean): Optional<WaterMeterReadingEntity>

    fun findByReadingDateBetweenAndActive(
        startDate: LocalDate,
        endDate: LocalDate,
        active: Boolean
    ): List<WaterMeterReadingEntity>
}
