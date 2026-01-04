package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.util.*

data class BillingConfigOutputDto(
    val id: UUID,
    val configKey: String,
    val configValue: BigDecimal,
    val description: String?
)

data class BillingConfigInputDto(
    val configKey: String,
    val configValue: BigDecimal,
    val description: String?
)

data class BillingConfigUpdateDto(
    val configValue: BigDecimal
)
