package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CashFlowTypeEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CashFlowTypeRepository : JpaRepository<CashFlowTypeEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableList<CashFlowTypeEntity>

    fun findByCodeAndActive(code: String, active: Boolean): Optional<CashFlowTypeEntity>

    fun findByNameAndActive(name: String, active: Boolean): Optional<CashFlowTypeEntity>
}
