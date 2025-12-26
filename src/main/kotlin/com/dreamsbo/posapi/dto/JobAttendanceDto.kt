package com.dreamsbo.posapi.dto

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

data class JobAttendanceOutputDto(
    val id: UUID,
    val jobId: UUID,
    val jobName: String,
    val partnerId: UUID,
    val partnerName: String,
    val partnerNumber: Long?,
    val partnerIdentificationNumber: String?,
    val attendanceDate: LocalDate,
    val present: Boolean,
    val checkInTime: OffsetDateTime?,
    val checkOutTime: OffsetDateTime?,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class JobAttendanceInputDto(
    val jobId: UUID,
    val partnerId: UUID,
    val attendanceDate: LocalDate,
    val present: Boolean = true,
    val checkInTime: OffsetDateTime? = null,
    val checkOutTime: OffsetDateTime? = null
)

data class JobAttendanceUpdateDto(
    val present: Boolean? = null,
    val checkInTime: OffsetDateTime? = null,
    val checkOutTime: OffsetDateTime? = null
)

data class JobAttendanceByDateDto(
    val attendanceDate: LocalDate,
    val attendances: List<JobAttendanceOutputDto>
)

data class BulkJobAttendanceInputDto(
    val jobId: UUID,
    val attendanceDate: LocalDate,
    val attendances: List<PartnerJobAttendanceDto>
)

data class PartnerJobAttendanceDto(
    val partnerId: UUID,
    val present: Boolean,
    val checkInTime: OffsetDateTime? = null,
    val checkOutTime: OffsetDateTime? = null
)

