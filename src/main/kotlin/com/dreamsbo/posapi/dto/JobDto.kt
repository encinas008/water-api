package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

data class JobOutputDto(
    val id: UUID,
    val name: String,
    val startDate: LocalDate,
    val description: String,
    val fine: BigDecimal?,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class JobInputDto(
    val name: String,
    val startDate: LocalDate,
    val description: String = "",
    val fine: java.math.BigDecimal? = null
)

data class JobUpdateDto(
    val name: String?,
    val startDate: LocalDate?,
    val description: String?,
    val fine: java.math.BigDecimal?
)
