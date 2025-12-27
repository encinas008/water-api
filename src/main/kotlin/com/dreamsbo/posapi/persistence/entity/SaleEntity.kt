package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.Generated
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "sale")
@NoArg
data class SaleEntity(
    @Id
    @Column(name = "sale_id")
    var id: UUID = UUID.randomUUID(),

    val observation: String = "",
    val discount: BigDecimal,
    val subTotal: BigDecimal,
    val total: BigDecimal,
    val moneyToBack: BigDecimal,

    @Column(name = "order_number", columnDefinition = "serial")
    @Generated
    val orderNumber: BigDecimal = BigDecimal.ZERO,

    @Column(name = "quantity_of_products")
    val quantityOfProducts: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_balance_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var cashBalance: CashBalanceEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var partner: PartnerEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var paymentType: PaymentTypeEntity,

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "sale",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderBy("createdAt DESC")
    val saleDetails: MutableList<SaleDetailEntity> = mutableListOf(),

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
