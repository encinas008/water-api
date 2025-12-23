package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.MeasurementEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MeasurementRepository : JpaRepository<MeasurementEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableList<MeasurementEntity>
}
