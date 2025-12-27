package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.JobEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JobRepository : JpaRepository<JobEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<JobEntity>
}


