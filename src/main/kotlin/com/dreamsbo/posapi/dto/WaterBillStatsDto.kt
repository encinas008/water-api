package com.dreamsbo.posapi.dto

import java.math.BigDecimal

data class WaterBillStatsDto(
    val totalBills: Long,
    val pendingBillsCount: Long,
    val overdueBillsCount: Long,
    val paidBillsCount: Long,
    val totalPendingAmount: BigDecimal,
    val totalOverdueAmount: BigDecimal,
    val totalPaidAmount: BigDecimal
)
