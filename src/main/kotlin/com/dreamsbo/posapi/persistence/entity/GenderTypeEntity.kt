package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "pos", name = "gender_type")
@NoArg
data class GenderTypeEntity(
    @Id
    @Column(name = "gender_type_id")
    var id: UUID = UUID.randomUUID(),

    var code: String = "",
    var name: String = "",

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
