package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

data class PartnerOutputDto(
    val id: UUID,
    val fullName: String,
    val partnerIdentificationNumber: String,
    val cel: String?,
    val address: String?,
    val waterConnectionNumber: String?,
    val waterMeterNumber: String?,
    val connectionStatusCode: String?,
    val connectionStatusName: String?,
    val connectionDate: LocalDate?,
    val waterConnectionAddress: String?,
    val currentDebt: BigDecimal,
    val lastBillingDate: LocalDate?,
    val notes: String?,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class PartnerInputDto(
    val fullName: String,
    val partnerIdentificationNumber: String,
    val cellphone: String = "",
    val address: String = "",
    val observation: String = "",
    val waterConnectionNumber: String? = null,
    val waterMeterNumber: String? = null,
    val connectionStatusCode: String? = null,
    val connectionDate: LocalDate? = null,
    val waterConnectionAddress: String? = null,
    val notes: String = ""
)

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
    val notes: String?
)

data class PartnerDebtSummaryDto(
    val partnerId: UUID,
    val partnerName: String,
    val waterConnectionNumber: String?,
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
