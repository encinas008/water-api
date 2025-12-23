package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.MeetingTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MeetingTypeRepository : JpaRepository<MeetingTypeEntity, UUID> {
    fun findByCodeAndActive(code: String, active: Boolean): Optional<MeetingTypeEntity>
    fun findAllByActive(active: Boolean): MutableList<MeetingTypeEntity>
}

