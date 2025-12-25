package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.JobAttendanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface JobAttendanceRepository : JpaRepository<JobAttendanceEntity, UUID> {

    fun findByJobIdAndActive(jobId: UUID, active: Boolean): List<JobAttendanceEntity>
    
    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean): List<JobAttendanceEntity>
    
    fun findByJobIdAndAttendanceDateAndActive(jobId: UUID, date: LocalDate, active: Boolean): List<JobAttendanceEntity>
    
    fun findByJobIdAndPartnerIdAndActive(jobId: UUID, partnerId: UUID, active: Boolean): List<JobAttendanceEntity>
    
    @Query("SELECT a FROM JobAttendanceEntity a WHERE a.job.id = :jobId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.active = :active")
    fun findByJobIdAndDateRange(
        @Param("jobId") jobId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("active") active: Boolean
    ): List<JobAttendanceEntity>
    
    fun findByJobIdAndPartnerIdAndAttendanceDateAndActive(
        jobId: UUID,
        partnerId: UUID,
        date: LocalDate,
        active: Boolean
    ): Optional<JobAttendanceEntity>
    
    @Query("SELECT a FROM JobAttendanceEntity a WHERE a.partner.id = :partnerId AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.active = :active AND a.job.active = :active AND a.present = false")
    fun findAbsencesByPartnerAndDateRange(
        @Param("partnerId") partnerId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("active") active: Boolean
    ): List<JobAttendanceEntity>
    
    // Obtener el primer registro de asistencia de un socio para un trabajo (para determinar si está asignado)
    @Query("SELECT a FROM JobAttendanceEntity a WHERE a.job.id = :jobId AND a.partner.id = :partnerId AND a.active = :active ORDER BY a.attendanceDate ASC")
    fun findFirstByJobIdAndPartnerId(@Param("jobId") jobId: UUID, @Param("partnerId") partnerId: UUID, @Param("active") active: Boolean): Optional<JobAttendanceEntity>
}

