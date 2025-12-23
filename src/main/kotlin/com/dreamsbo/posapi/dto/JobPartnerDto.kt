package com.dreamsbo.posapi.dto

import java.time.OffsetDateTime
import java.util.*

data class JobPartnerOutputDto(
    val id: UUID,
    val jobId: UUID,
    val jobName: String,
    val partnerId: UUID,
    val partnerName: String,
    val partnerIdentificationNumber: String,
    val active: Boolean,
    val createdAt: OffsetDateTime
)

data class AssignPartnersToJobDto(
    val partnerIds: List<UUID>
)

data class JobPartnerAssignmentDto(
    val jobId: UUID,
    val jobName: String,
    val assignedPartners: List<PartnerAssignmentInfoDto>
)

data class PartnerAssignmentInfoDto(
    val partnerId: UUID,
    val partnerNumber: Long?,
    val partnerName: String,
    val partnerIdentificationNumber: String,
    val isAssigned: Boolean,
    val assignmentId: UUID?
)
