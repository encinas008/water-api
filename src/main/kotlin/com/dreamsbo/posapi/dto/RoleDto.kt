package com.dreamsbo.posapi.dto

import java.util.UUID

data class RoleOutputDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val code: String
)
