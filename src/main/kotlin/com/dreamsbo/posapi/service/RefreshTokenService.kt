package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.ExpiredRefreshTokenException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.persistence.entity.RefreshTokenEntity
import com.dreamsbo.posapi.persistence.repository.RefreshTokenRepository
import com.dreamsbo.posapi.persistence.repository.UserRepository
import com.dreamsbo.posapi.security.SecurityApplicationProperty
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import org.springframework.stereotype.Service

val ZONE_ID: ZoneId = ZoneId.of("America/La_Paz")

@Service
class RefreshTokenService(
    val refreshTokenRepository: RefreshTokenRepository,
    val userRepository: UserRepository,
    val securityApplicationProperty: SecurityApplicationProperty,
) {

    fun refreshToken(username: String?, refreshTokenId: UUID?): RefreshTokenEntity {

        val userEntity = userRepository.findByUsername(username).get()

        val expirationTime = ZonedDateTime.now(ZONE_ID).plus(
            securityApplicationProperty.securityAuthenticationJwtRefreshTokenValidityInSeconds,
            ChronoUnit.SECONDS
        )

        if (refreshTokenId != null) {

            val refreshTokenEntity = refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow { NotFoundEntityException("Has not been found refreshToken. RefreshTokenId = $refreshTokenId") }

            refreshTokenEntity.expireDate = expirationTime

            return refreshTokenRepository.save(refreshTokenEntity)
        }

        val refreshTokenEntity = RefreshTokenEntity(
            id = UUID.randomUUID(),
            user = userEntity,
            expireDate = expirationTime
        )

        return refreshTokenRepository.save(refreshTokenEntity)
    }

    fun findByToken(refreshTokenId: UUID): Optional<RefreshTokenEntity> {

        return refreshTokenRepository.findById(refreshTokenId)
    }

    fun verifyExpiration(refreshTokenEntity: RefreshTokenEntity): RefreshTokenEntity {

        val expirationDate = refreshTokenEntity.expireDate.withZoneSameInstant(ZONE_ID)
        val currentTime = ZonedDateTime.now()

        if (expirationDate < currentTime) {

            refreshTokenRepository.delete(refreshTokenEntity)

            throw ExpiredRefreshTokenException("${refreshTokenEntity.id} Refresh token has been expired. Please signIn again!")
        }

        return refreshTokenEntity;
    }
}
