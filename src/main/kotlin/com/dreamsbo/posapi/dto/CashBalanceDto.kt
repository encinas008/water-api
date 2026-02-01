package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class CashBalanceInputDto(
    val moneyToOpenCashBalance: BigDecimal,
    val userId: UUID,
)

data class CloseCashBalanceInputDto(
    val cashBalanceId: UUID,
)

data class CashBalanceOutputDto(
    val id: UUID,
    val description: String,
    val assignee: String,
    val openTime: OffsetDateTime,
    val closeTime: OffsetDateTime?,
    val initialMoney: BigDecimal,

    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class CashFromSaleDetails(
    val cash: BigDecimal,
    val qr: BigDecimal,
    val transference: BigDecimal,
)

data class CashFromCashFlowsDetails(
    val cashIn: BigDecimal,
    val cashQrIn: BigDecimal,
    val cashTransferIn: BigDecimal,
    val cashOut: BigDecimal,
    val cashQrOut: BigDecimal,
    val cashTransferOut: BigDecimal,
)

data class CashBalanceDetails(
    val totalCashFromSales: BigDecimal,
    val cashFromSalesInCash: BigDecimal,
    val cashFromSalesInOthers: BigDecimal,

    val totalCash: BigDecimal,
    val totalOthers: BigDecimal,
    val totalCashInBox: BigDecimal,
)

data class CashBalanceDetailsOutputDto(
    val id: UUID,
    val description: String,
    val assignee: String,
    val boxName: String,
    val openTime: OffsetDateTime,
    val closeTime: OffsetDateTime?,
    val initialMoney: BigDecimal,

    val cashFromSalesDetails: CashFromSaleDetails,
    val cashFromCashFlowsDetails: CashFromCashFlowsDetails,
    val cashBalanceDetails: CashBalanceDetails,

    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class CashBalanceMovementDto(
    val id: UUID,
    val type: String, // "INGRESO" or "EGRESO"
    val category: String, // "COBRANZA" or "MANUAL"
    val description: String,
    val amount: BigDecimal,
    val paymentMethod: String, // "EFECTIVO", "QR", etc.
    val date: OffsetDateTime,
    val reference: String? = null, // receipt number for payments
    val billNumber: String? = null,
    val partnerName: String? = null,
    val partnerNumber: String? = null,
    val billingPeriod: String? = null, // e.g. "Enero 2025"
    val correlativeNumber: Int? = null
)

data class CashBalanceMovementsOutputDto(
    val cashBalanceId: UUID,
    val movements: List<CashBalanceMovementDto>
)

