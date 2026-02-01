package com.dreamsbo.posapi.dto

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

data class MeetingAttendanceOutputDto(
    val id: UUID,
    val meetingId: UUID,
    val meetingName: String,
    val partnerId: UUID,
    val partnerName: String,
    val partnerNumber: Long?,
    val partnerIdentificationNumber: String?,
    val attendanceDate: LocalDate,
    val present: Boolean,
    val checkInTime: OffsetDateTime?,
    val checkOutTime: OffsetDateTime?,
    val lateFine: java.math.BigDecimal,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class MeetingAttendanceInputDto(
    val meetingId: UUID,
    val partnerId: UUID,
    val attendanceDate: LocalDate,
    val present: Boolean,
    val checkInTime: OffsetDateTime?,
    val checkOutTime: OffsetDateTime?
)

data class MeetingAttendanceUpdateDto(
    val present: Boolean?,
    val checkInTime: OffsetDateTime?,
    val checkOutTime: OffsetDateTime?
)

data class MeetingAttendanceByDateDto(
    val attendanceDate: LocalDate,
    val attendances: List<MeetingAttendanceOutputDto>
)

data class BulkMeetingAttendanceInputDto(
    val meetingId: UUID,
    val attendanceDate: LocalDate,
    val attendances: List<PartnerMeetingAttendanceDto>
)

data class PartnerMeetingAttendanceDto(
    val partnerId: UUID,
    val present: Boolean,
    val checkInTime: OffsetDateTime? = null,
    val checkOutTime: OffsetDateTime? = null
)

