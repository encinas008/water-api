package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class PartnerOutputDto(
    val id: UUID,
    val partnerNumber: Long?,
    val fullName: String,
    val partnerIdentificationNumber: String?,
    val cel: String?,
    val address: String?,
    val waterMeterNumber: String?,
    val connectionStatusCode: String?,
    val connectionStatusName: String?,
    val connectionDate: LocalDate?,
    val waterConnectionAddress: String?,
    val currentDebt: BigDecimal,
    val lastBillingDate: LocalDate?,
    val isElderly: Boolean,
    val elderlyPaysMeetingFines: Boolean,
    val elderlyMeetingFineExplanation: String?,
    val elderlyPaysJobFines: Boolean,
    val elderlyJobFineExplanation: String?,
    val notes: String?,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val lastPaymentId: UUID? = null
)

data class PartnerInputDto(
    val fullName: String,
    val partnerIdentificationNumber: String? = null,
    val cellphone: String = "",
    val address: String = "",
    val observation: String = "",
    val waterMeterNumber: String? = null,
    val connectionStatusCode: String? = null,
    val connectionDate: LocalDate? = null,
    val waterConnectionAddress: String? = null,
    val isElderly: Boolean = false,
    val elderlyPaysMeetingFines: Boolean = true,
    val elderlyMeetingFineExplanation: String? = null,
    val elderlyPaysJobFines: Boolean = true,
    val elderlyJobFineExplanation: String? = null,
    val notes: String = "",
    val installationAmount: BigDecimal = BigDecimal.ZERO,
    val paymentTypeId: UUID? = null,
    val cashBalanceId: UUID? = null,
    val userId: UUID? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PartnerUpdateDto(
    val fullName: String?,
    val partnerIdentificationNumber: String?,
    val cellphone: String?,
    val address: String?,
    val observation: String?,
    val waterConnectionNumber: String?,
    val waterMeterNumber: String?,
    val connectionStatusCode: String?,
    val connectionDate: LocalDate?,
    val waterConnectionAddress: String?,
    val isElderly: Boolean?,
    val elderlyPaysMeetingFines: Boolean?,
    val elderlyMeetingFineExplanation: String?,
    val elderlyPaysJobFines: Boolean?,
    val elderlyJobFineExplanation: String?,
    val notes: String?
)

data class PartnerDebtSummaryDto(
    val partnerId: UUID,
    val partnerName: String,
    val currentDebt: BigDecimal,
    val pendingBills: Int,
    val overdueBills: Int,
    val totalPendingAmount: BigDecimal,
    val lastPaymentDate: LocalDate?,
    val lastBillingDate: LocalDate?,
    val connectionStatus: String?
)

data class ConnectionStatusUpdateDto(
    val connectionStatusCode: String
)
