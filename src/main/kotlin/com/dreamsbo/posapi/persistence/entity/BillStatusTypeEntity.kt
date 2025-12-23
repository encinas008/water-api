package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "bill_status_type")
@NoArg
data class BillStatusTypeEntity(
    @Id
    @Column(name = "bill_status_type_id")
    var id: UUID = UUID.randomUUID(),

    var code: String,
    var name: String,
    var description: String = "",

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
