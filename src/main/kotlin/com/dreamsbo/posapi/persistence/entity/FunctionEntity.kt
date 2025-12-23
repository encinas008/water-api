package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/**
 * ABAC(Attribute-Based Access Control)
 */
@Entity
@Table(schema = "pos", name = "abac_function")
@NoArg
data class FunctionEntity(
    @Id
    @Column(name = "function_id")
    var id: UUID = UUID.randomUUID(),

    var code: String = "",
    var name: String = "",
    var description: String = "",

    @OneToMany(mappedBy = "function")
    var roleFunction: MutableList<RoleFunctionEntity>,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
