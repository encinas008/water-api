package com.dreamsbo.posapi.security.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.ExpiredTokenException
import com.dreamsbo.posapi.dto.AuthenticationOutputDto
import com.dreamsbo.posapi.persistence.entity.UserEntity
import com.dreamsbo.posapi.security.SecurityApplicationProperty
import com.dreamsbo.posapi.security.SecurityConstants
import com.dreamsbo.posapi.service.RefreshTokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import kotlin.RuntimeException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class JwtService(
    val securityApplicationProperty: SecurityApplicationProperty,
    val refreshTokenService: RefreshTokenService,
) {

    fun generateToken(
        user: UserEntity,
        expirationToken: Long,
        refreshToken: UUID?,
    ): AuthenticationOutputDto {

        val claims: MutableMap<String?, Any> = mutableMapOf()
        claims[SecurityConstants.USER_ID_FIELD] = user.id
        claims[SecurityConstants.NAME_FIELD] = user.profile.name
        claims[SecurityConstants.LAST_NAME_FIELD] = user.profile.lastname
        claims[SecurityConstants.GENDER_FIELD] = user.profile.gender.name
        claims[SecurityConstants.ROLE_FIELD] = user.userRole?.first()?.role?.name
            ?: throw BadRequestException("No roles assigned to user. UserId = ${user.id}")

        return createToken(claims, user.username, expirationToken, refreshToken)
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {

        val username: String = extractUsername(token)

        return username == userDetails.username && !isTokenExpired(token)
    }

    fun extractAllClaims(token: String): Claims {

        try {
            val jwtParser: JwtParser = Jwts.parser().verifyWith(secretKey()).build()

            return jwtParser.parseSignedClaims(token).payload
        } catch (e: ExpiredJwtException) {

            throw ExpiredTokenException("Expired session. Token = $token")
        } catch (e: Exception) {

            throw RuntimeException()
        }
    }

    fun extractUsername(token: String): String {

        return extractAllClaims(token).subject
    }

    private fun extractExpiration(token: String): Date {

        return extractAllClaims(token).expiration
    }

    private fun isTokenExpired(token: String): Boolean {

        return extractExpiration(token).before(Date())
    }


    private fun createToken(
        claims: MutableMap<String?, Any>,
        username: String?,
        expirationToken: Long,
        refreshToken: UUID?,
    ): AuthenticationOutputDto {

        val tokenExpiration = Instant.now().plusSeconds(expirationToken).toEpochMilli()

        val refreshTokenEntity = refreshTokenService.refreshToken(username, refreshToken)

        val accessToken = Jwts.builder().claims(claims).subject(username)
            .issuedAt(Date(Instant.now().toEpochMilli()))
            .expiration(Date(tokenExpiration)).signWith(secretKey())
            .compact()

        return AuthenticationOutputDto(token = accessToken, refreshToken = refreshTokenEntity.id)
    }

    private fun secretKey(): SecretKey {

        return Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(securityApplicationProperty.securityAuthenticationJwtSecret)
        )
    }
}
