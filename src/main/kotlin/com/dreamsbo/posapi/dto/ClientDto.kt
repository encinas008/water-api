package com.dreamsbo.posapi.dto

import java.time.OffsetDateTime
import java.util.*

data class ClientOutputDto(
    val id: UUID,
    val name: String,
    val documentIdentifier: String,
    val cel: String?,

    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)
