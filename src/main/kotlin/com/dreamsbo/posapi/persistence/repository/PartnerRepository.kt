package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.PartnerEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PartnerRepository : JpaRepository<PartnerEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<PartnerEntity>
    
    fun findByWaterMeterNumberAndActive(waterMeterNumber: String, active: Boolean): Optional<PartnerEntity>
}
