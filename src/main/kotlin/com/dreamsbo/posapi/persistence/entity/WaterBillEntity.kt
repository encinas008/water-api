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
@Table(schema = "pos", name = "water_bill")
@NoArg
data class WaterBillEntity(
    @Id
    @Column(name = "water_bill_id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "bill_number")
    var billNumber: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var partner: PartnerEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var reading: WaterMeterReadingEntity? = null,

    @Column(name = "billing_period_start")
    var billingPeriodStart: LocalDate,

    @Column(name = "billing_period_end")
    var billingPeriodEnd: LocalDate,

    @Column(name = "consumption_m3")
    var consumptionM3: BigDecimal,

    @Column(name = "rate_per_m3")
    var ratePerM3: BigDecimal,

    @Column(name = "base_amount")
    var baseAmount: BigDecimal,

    @Column(name = "total_amount")
    var totalAmount: BigDecimal,

    @Column(name = "paid_amount")
    var paidAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "remaining_balance")
    var remainingBalance: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_status_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var status: BillStatusTypeEntity,

    @Column(name = "due_date")
    var dueDate: LocalDate,

    @Column(name = "paid_date")
    var paidDate: LocalDate? = null,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
