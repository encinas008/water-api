package com.dreamsbo.posapi.dto

import java.time.OffsetDateTime
import java.util.*

data class MeetingPartnerOutputDto(
    val id: UUID,
    val meetingId: UUID,
    val meetingName: String,
    val partnerId: UUID,
    val partnerName: String,
    val partnerNumber: Long?,
    val partnerIdentificationNumber: String,
    val active: Boolean,
    val createdAt: OffsetDateTime
)

data class AssignPartnersToMeetingDto(
    val partnerIds: List<UUID>
)

data class MeetingPartnerAssignmentDto(
    val meetingId: UUID,
    val meetingName: String,
    val assignedPartners: List<PartnerAssignmentInfoDto>
)

