package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "partner")
@NoArg
data class PartnerEntity(
    @Id
    @Column(name = "partner_id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "full_name")
    var fullName: String,

    @Column(name = "partner_identification_number")
    var partnerIdentificationNumber: String,

    var cellphone: String = "",
    var observation: String = "",
    var address: String = "",

    // Water connection fields
    @Column(name = "water_connection_number")
    var waterConnectionNumber: String? = null,

    @Column(name = "water_meter_number")
    var waterMeterNumber: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_status_type_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var connectionStatus: ConnectionStatusTypeEntity? = null,

    @Column(name = "connection_date")
    var connectionDate: LocalDate? = null,

    @Column(name = "water_connection_address")
    var waterConnectionAddress: String? = null,

    @Column(name = "current_debt")
    var currentDebt: BigDecimal = BigDecimal.ZERO,

    @Column(name = "last_billing_date")
    var lastBillingDate: LocalDate? = null,

    var notes: String = "",

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
