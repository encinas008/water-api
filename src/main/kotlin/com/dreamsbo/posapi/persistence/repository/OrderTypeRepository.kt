package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.OrderTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderTypeRepository : JpaRepository<OrderTypeEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableList<OrderTypeEntity>
}
