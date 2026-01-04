package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.BillingConfigEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BillingConfigRepository : JpaRepository<BillingConfigEntity, UUID> {
    
    fun findByConfigKeyAndActive(configKey: String, active: Boolean): Optional<BillingConfigEntity>
    
    fun findAllByActive(active: Boolean): List<BillingConfigEntity>
}
