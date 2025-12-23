package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.AttendanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface AttendanceRepository : JpaRepository<AttendanceEntity, UUID> {

    fun findByJobIdAndActive(jobId: UUID, active: Boolean): List<AttendanceEntity>
    
    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean): List<AttendanceEntity>
    
    fun findByJobIdAndAttendanceDateAndActive(jobId: UUID, date: LocalDate, active: Boolean): List<AttendanceEntity>
    
    fun findByJobIdAndPartnerIdAndActive(jobId: UUID, partnerId: UUID, active: Boolean): List<AttendanceEntity>
    
    @Query("SELECT a FROM AttendanceEntity a WHERE a.job.id = :jobId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.active = :active")
    fun findByJobIdAndDateRange(
        @Param("jobId") jobId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("active") active: Boolean
    ): List<AttendanceEntity>
    
    fun findByJobIdAndPartnerIdAndAttendanceDateAndActive(
        jobId: UUID,
        partnerId: UUID,
        date: LocalDate,
        active: Boolean
    ): Optional<AttendanceEntity>
}

