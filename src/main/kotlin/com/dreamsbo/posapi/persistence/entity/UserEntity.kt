package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime
import java.util.UUID

/**
 * ABAC(Attribute-Based Access Control) - User
 */
@Entity
@Table(schema = "pos", name = "abac_user")
@NoArg
data class UserEntity(
    @Id
    @Column(name = "user_id")
    var id: UUID = UUID.randomUUID(),

    var username: String = "",
    @Column(name = "passwd")
    var password: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var profile: ProfileEntity,

    @OneToMany(mappedBy = "user")
    var userRole: MutableList<UserRoleEntity>?,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
