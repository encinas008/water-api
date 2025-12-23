package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime

/**
 * ABAC(Attribute-Based Access Control)  Role-Function
 */
@Entity
@Table(schema = "pos", name = "abac_role_function")
@NoArg
data class RoleFunctionEntity(
    @Id
    @Column(name = "role_function_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val role: RoleEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "function_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val function: FunctionEntity,

    val create: Boolean = false,
    val update: Boolean = false,
    val read: Boolean = false,
    val delete: Boolean = false,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
