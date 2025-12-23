package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.BillStatusTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BillStatusTypeRepository : JpaRepository<BillStatusTypeEntity, UUID> {

    fun findByCodeAndActive(code: String, active: Boolean): Optional<BillStatusTypeEntity>

    fun findByNameAndActive(name: String, active: Boolean): Optional<BillStatusTypeEntity>
}
