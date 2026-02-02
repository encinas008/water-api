package com.dreamsbo.posapi.dto

import java.math.BigDecimal

data class DailyMovementReportDto(
    val concepts: List<MovementConceptDto>,
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val grandTotal: BigDecimal
)

data class MovementConceptDto(
    val name: String,
    val type: String, // "INGRESO" or "EGRESO"
    val amount: BigDecimal,
    val isManual: Boolean = false
)
