package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.MeetingEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MeetingRepository : JpaRepository<MeetingEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<MeetingEntity>
    
    fun countByActive(active: Boolean): Long
    
    fun findAllByActive(active: Boolean, pageable: Pageable): Page<MeetingEntity>
    
    @Query("""
        SELECT m FROM MeetingEntity m 
        WHERE m.active = :active 
        AND (
            LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findAllByActiveAndSearch(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<MeetingEntity>

    fun findAllByActiveAndMeetingDate(active: Boolean, meetingDate: java.time.LocalDate, pageable: Pageable): Page<MeetingEntity>

    @Query("""
        SELECT m FROM MeetingEntity m 
        WHERE m.active = :active 
        AND m.meetingDate = :meetingDate
        AND (
            LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findAllByActiveAndSearchAndMeetingDate(
        @Param("active") active: Boolean,
        @Param("search") search: String,
        @Param("meetingDate") meetingDate: java.time.LocalDate,
        pageable: Pageable
    ): Page<MeetingEntity>

    @Query("SELECT DISTINCT m.meetingDate FROM MeetingEntity m WHERE m.active = :active")
    fun findDistinctMeetingDatesByActive(@Param("active") active: Boolean): List<java.time.LocalDate>
}



