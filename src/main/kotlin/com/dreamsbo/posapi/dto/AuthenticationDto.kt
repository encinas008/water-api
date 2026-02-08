package com.dreamsbo.posapi.dto

import java.util.UUID

data class AuthenticationInputDto(val username: String, val password: String)

data class AuthenticationOutputDto(val token: String, val refreshToken: UUID)

data class RefreshTokenInputDto(val refreshTokenId: UUID)
 
data class ChangePasswordInputDto(
    val currentPassword: String,
    val newPassword: String
)
