package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.PartnerEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PartnerRepository : JpaRepository<PartnerEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<PartnerEntity>
    
    fun findByWaterMeterNumberAndActive(waterMeterNumber: String, active: Boolean): Optional<PartnerEntity>
    
    fun findAllByActive(active: Boolean, pageable: Pageable): Page<PartnerEntity>
    
    @Query("""
        SELECT p FROM PartnerEntity p 
        WHERE p.active = :active 
        AND CAST(p.partnerNumber AS string) = :search
    """)
    fun findAllByActiveAndSearch(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<PartnerEntity>

    @Query("""
        SELECT p FROM PartnerEntity p 
        WHERE p.active = true 
        AND CAST(p.partnerNumber AS string) = :query
    """)
    fun searchByTerm(@Param("query") query: String): List<PartnerEntity>
}
