package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.JobEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface JobRepository : JpaRepository<JobEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<JobEntity>
    
    fun findAllByActive(active: Boolean, pageable: Pageable): Page<JobEntity>
    
    @Query("""
        SELECT j FROM JobEntity j 
        WHERE j.active = :active 
        AND (
            LOWER(j.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(j.description) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findAllByActiveAndSearch(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<JobEntity>
}


