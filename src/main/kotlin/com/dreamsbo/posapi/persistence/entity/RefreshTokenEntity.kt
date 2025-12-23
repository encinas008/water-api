package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "abac_refresh_token")
@NoArg
data class RefreshTokenEntity(
    @Id
    @Column(name = "refresh_token_id")
    var id: UUID = UUID.randomUUID(),

    @Temporal(TemporalType.TIMESTAMP)
    var expireDate: ZonedDateTime,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
