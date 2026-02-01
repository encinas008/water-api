package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "cash_flow")
@NoArg
data class CashFlowEntity(
    @Id
    @Column(name = "cash_flow_id")
    var id: UUID = UUID.randomUUID(),

    val amount: BigDecimal,
    val description: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cash_balance_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var cashBalance: CashBalanceEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cash_flow_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var cashFlowType: CashFlowTypeEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var paymentType: PaymentTypeEntity,

    @Column(name = "correlative_number")
    var correlativeNumber: Int? = null,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
