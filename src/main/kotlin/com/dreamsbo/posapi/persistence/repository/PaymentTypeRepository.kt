package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.PaymentTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentTypeRepository : JpaRepository<PaymentTypeEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableSet<PaymentTypeEntity>
}
