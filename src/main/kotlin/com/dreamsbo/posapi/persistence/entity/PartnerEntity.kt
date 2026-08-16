package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Entity
@Table(schema = "pos", name = "partner")
@NoArg
data class PartnerEntity(
    @Id
    @Column(name = "partner_id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "partner_number", unique = true, insertable = false, updatable = false)
    var partnerNumber: Long? = null,

    @Column(name = "full_name")
    var fullName: String,

    @Column(name = "partner_identification_number")
    var partnerIdentificationNumber: String? = null,

    var cellphone: String = "",
    var observation: String = "",
    var address: String = "",

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

    @Column(name = "is_elderly")
    var isElderly: Boolean = false,

    @Column(name = "elderly_pays_meeting_fines")
    var elderlyPaysMeetingFines: Boolean = true,

    @Column(name = "elderly_meeting_fine_explanation")
    var elderlyMeetingFineExplanation: String? = null,

    @Column(name = "elderly_pays_job_fines")
    var elderlyPaysJobFines: Boolean = true,

    @Column(name = "elderly_job_fine_explanation")
    var elderlyJobFineExplanation: String? = null,

    var notes: String = "",

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),

    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ")
    var updatedAt: OffsetDateTime? = null,

    @Column(name = "status_changed_at", columnDefinition = "TIMESTAMPTZ")
    var statusChangedAt: OffsetDateTime? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,
)
