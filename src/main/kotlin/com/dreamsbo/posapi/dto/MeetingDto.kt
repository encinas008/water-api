package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

data class MeetingOutputDto(
    val id: UUID,
    val name: String,
    val meetingDate: LocalDate,
    val hour: Int, // 1-12
    val minute: Int, // 0-59
    val amPm: String, // "AM" o "PM"
    val meetingTypeCode: String?,
    val meetingTypeName: String?,
    val description: String,
    val fine: BigDecimal,
    val waitingMinutes: Int,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val locked: Boolean
)

data class MeetingInputDto(
    val name: String,
    val meetingDate: LocalDate,
    val hour: Int, // 1-12
    val minute: Int, // 0-59
    val amPm: String, // "AM" o "PM"
    val meetingTypeCode: String? = null,
    val description: String? = null,
    val fine: BigDecimal,
    val waitingMinutes: Int = 0,
    val partnerIds: List<UUID>? = null
)

data class MeetingUpdateDto(
    val name: String?,
    val meetingDate: LocalDate?,
    val hour: Int?,
    val minute: Int?,
    val amPm: String?,
    val meetingTypeCode: String?,
    val description: String?,
    val fine: BigDecimal,
    val waitingMinutes: Int? = null
)

