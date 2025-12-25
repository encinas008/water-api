package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.MeetingAttendanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface MeetingAttendanceRepository : JpaRepository<MeetingAttendanceEntity, UUID> {
    fun findByMeetingIdAndActive(meetingId: UUID, active: Boolean): MutableList<MeetingAttendanceEntity>
    fun findByPartnerIdAndActive(partnerId: UUID, active: Boolean): MutableList<MeetingAttendanceEntity>
    fun findByMeetingIdAndAttendanceDateAndActive(meetingId: UUID, date: LocalDate, active: Boolean): MutableList<MeetingAttendanceEntity>

    @Query("SELECT ma FROM MeetingAttendanceEntity ma WHERE ma.meeting.id = :meetingId AND ma.attendanceDate >= :startDate AND ma.attendanceDate <= :endDate AND ma.active = :active")
    fun findByMeetingIdAndDateRange(meetingId: UUID, startDate: LocalDate, endDate: LocalDate, active: Boolean): MutableList<MeetingAttendanceEntity>

    @Query("SELECT ma FROM MeetingAttendanceEntity ma WHERE ma.meeting.id = :meetingId AND ma.partner.id = :partnerId AND ma.attendanceDate = :date AND ma.active = :active")
    fun findByMeetingIdAndPartnerIdAndDate(meetingId: UUID, partnerId: UUID, date: LocalDate, active: Boolean): Optional<MeetingAttendanceEntity>
    
    @Query("SELECT ma FROM MeetingAttendanceEntity ma WHERE ma.partner.id = :partnerId AND ma.attendanceDate BETWEEN :startDate AND :endDate AND ma.active = :active AND ma.meeting.active = :active AND ma.present = false")
    fun findAbsencesByPartnerAndDateRange(
        @Param("partnerId") partnerId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("active") active: Boolean
    ): List<MeetingAttendanceEntity>
    
    // Obtener registros de asistencia únicos por socio (para determinar asignaciones)
    @Query("SELECT DISTINCT ma.partner.id FROM MeetingAttendanceEntity ma WHERE ma.meeting.id = :meetingId AND ma.active = :active")
    fun findDistinctPartnerIdsByMeetingId(@Param("meetingId") meetingId: UUID, @Param("active") active: Boolean): List<UUID>
    
    // Obtener el primer registro de asistencia de un socio para una reunión (para determinar si está asignado)
    @Query("SELECT ma FROM MeetingAttendanceEntity ma WHERE ma.meeting.id = :meetingId AND ma.partner.id = :partnerId AND ma.active = :active ORDER BY ma.attendanceDate ASC")
    fun findFirstByMeetingIdAndPartnerId(@Param("meetingId") meetingId: UUID, @Param("partnerId") partnerId: UUID, @Param("active") active: Boolean): Optional<MeetingAttendanceEntity>
}

