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
@Table(schema = "pos", name = "water_payment")
@NoArg
data class WaterPaymentEntity(
    @Id
    @Column(name = "water_payment_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "water_bill_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var waterBill: WaterBillEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var partner: PartnerEntity,

    @Column(name = "payment_date")
    var paymentDate: LocalDate,

    var amount: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var paymentType: PaymentTypeEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_balance_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var cashBalance: CashBalanceEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: UserEntity,

    @Column(name = "receipt_number")
    var receiptNumber: String,

    var observation: String = "",

    @Column(name = "correlative_number")
    var correlativeNumber: Int? = null,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
