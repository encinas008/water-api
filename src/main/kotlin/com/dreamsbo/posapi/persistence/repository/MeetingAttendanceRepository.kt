package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.MeetingAttendanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
}

