package com.dreamsbo.posapi.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SecurityApplicationProperty {

    @Value("\${security.authentication.jwt.secret}")
    val securityAuthenticationJwtSecret: String = ""

    @Value("\${security.authentication.jwt.token-validity-in-seconds}")
    val securityAuthenticationJwtTokenValidityInSeconds: Long = 0

    @Value("\${security.authentication.jwt.refresh-token-validity-in-seconds}")
    val securityAuthenticationJwtRefreshTokenValidityInSeconds: Long = 0
}
