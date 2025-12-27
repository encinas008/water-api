package com.dreamsbo.posapi.dto

import java.util.*

data class MeetingTypeOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val active: Boolean
)



