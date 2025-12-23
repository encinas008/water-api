package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.BoxEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BoxRepository : JpaRepository<BoxEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<BoxEntity>

    @Query("Select c from BoxEntity c where c.user.id = :userId AND c.active = :active")
    fun findByUserId(userId: UUID, active: Boolean): Optional<BoxEntity>
}
