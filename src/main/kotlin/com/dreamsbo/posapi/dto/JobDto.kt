package com.dreamsbo.posapi.dto

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

data class JobOutputDto(
    val id: UUID,
    val name: String,
    val startDate: LocalDate,
    val description: String,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class JobInputDto(
    val name: String,
    val startDate: LocalDate,
    val description: String = ""
)

data class JobUpdateDto(
    val name: String?,
    val startDate: LocalDate?,
    val description: String?
)
