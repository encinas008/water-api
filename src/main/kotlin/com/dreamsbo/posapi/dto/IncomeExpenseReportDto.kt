package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.LocalDate

data class IncomeReportDto(
    val items: List<IncomeReportItemDto>,
    val totalIncome: BigDecimal
)

data class IncomeReportItemDto(
    val nro: Int,
    val fecha: LocalDate,
    val nombreSocio: String,
    val numeroSocio: Long,
    val tarifaBasica: BigDecimal,
    val aporteOtb: BigDecimal,
    val aporteDeporte: BigDecimal,
    val otros: BigDecimal,
    val total: BigDecimal,
    val responsable: String
)

data class ExpenseReportDto(
    val items: List<ExpenseReportItemDto>,
    val totalExpense: BigDecimal
)

data class ExpenseReportItemDto(
    val nro: Int,
    val fecha: LocalDate,
    val detalle: String,
    val total: BigDecimal,
    val responsable: String
)

data class WaivedReportDto(
    val items: List<WaivedReportItemDto>,
    val totalWaived: BigDecimal
)

data class WaivedReportItemDto(
    val nro: Int,
    val fecha: LocalDate,
    val nombreSocio: String,
    val numeroSocio: Long,
    val mesFacturado: String,
    val totalCondonado: BigDecimal,
    val responsable: String
)
