package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "client")
@NoArg
data class ClientEntity(
    @Id
    @Column(name = "client_id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "full_name")
    var fullName: String,

    @Column(name = "client_identification_number")
    var clientIdentificationNumber: String,

    var cellphone: String = "",
    var observation: String = "",
    var address: String = "",

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
