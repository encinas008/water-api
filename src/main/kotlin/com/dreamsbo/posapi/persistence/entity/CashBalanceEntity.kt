package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "cash_balance")
@NoArg
data class CashBalanceEntity(
    @Id
    @Column(name = "cash_balance_id")
    var id: UUID = UUID.randomUUID(),

    val description: String = "",

    @Column(name = "open_time")
    val openTime: OffsetDateTime,

    @Column(name = "close_time")
    var closeTime: OffsetDateTime? = null,

    @Column(name = "initial_money")
    val initialMoney: BigDecimal,

    @Column(name = "cash_in_box")
    var cashInBox: BigDecimal? = null,

    @Column(name = "cash_difference")
    var cashDifference: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "box_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var box: BoxEntity,

    @Column(name = "last_correlative")
    var lastCorrelative: Int = 0,

    @Column(name = "last_correlative_expense")
    var lastCorrelativeExpense: Int = 0,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
