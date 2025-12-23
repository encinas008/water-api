package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.AuthenticationInputDto
import com.dreamsbo.posapi.dto.AuthenticationOutputDto
import com.dreamsbo.posapi.dto.RefreshTokenInputDto
import com.dreamsbo.posapi.persistence.entity.RefreshTokenEntity
import com.dreamsbo.posapi.security.SecurityApplicationProperty
import com.dreamsbo.posapi.security.service.JwtService
import com.dreamsbo.posapi.service.RefreshTokenService
import com.dreamsbo.posapi.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class AuthController(
    val userService: UserService,
    val refreshTokenService: RefreshTokenService,
    val jwtService: JwtService,
    val securityApplicationProperty: SecurityApplicationProperty,
) {

    @PostMapping("/auth/sign-in")
    fun signIn(@RequestBody authenticationInputDto: AuthenticationInputDto): AuthenticationOutputDto {

        return userService.signIn(authenticationInputDto)
    }

    @PostMapping("/auth/refresh-token")
    fun refreshToken(@RequestBody refreshTokenInputDto: RefreshTokenInputDto): AuthenticationOutputDto {

        return refreshTokenService.findByToken(refreshTokenInputDto.refreshTokenId)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshTokenEntity::user)
            .map {

                jwtService.generateToken(
                    it,
                    securityApplicationProperty.securityAuthenticationJwtTokenValidityInSeconds,
                    refreshTokenInputDto.refreshTokenId
                )
            }.orElseThrow {

                NotFoundEntityException("Refresh token has been not found. RefreshTokenId = ${refreshTokenInputDto.refreshTokenId}")
            }
    }
}
