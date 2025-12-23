package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class CashFlowInputDto(
    val paymentTypeId: UUID,
    val cashFlowTypeId: UUID,
    val amount: BigDecimal,
    val description: String,
    val userId: UUID,
)

data class CashFlowOutputDto(
    val id: UUID,
    val box: String,
    val boxId: UUID,
    val assignee: String,
    val type: String,
    val description: String,
    val amount: BigDecimal,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)
