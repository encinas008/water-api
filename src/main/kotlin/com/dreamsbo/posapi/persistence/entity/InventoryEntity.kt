package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "inventory")
@NoArg
data class InventoryEntity(
    @Id
    @Column(name = "inventory_id")
    var id: UUID = UUID.randomUUID(),

    val detail: String? = "",

    @Column(name = "quantity_in")
    val quantityIn: Double = 0.0,

    @Column(name = "cost_unit_in")
    val costUnitIn: Double = 0.0,

    @Column(name = "total_in")
    val totalIn: Double = 0.0,

    @Column(name = "quantity_out")
    val quantityOut: Double = 0.0,

    @Column(name = "cost_unit_out")
    val costUnitOut: Double = 0.0,

    @Column(name = "total_out")
    val totalOut: Double = 0.0,

    @Column(name = "quantity_in_stock_on_hand")
    val quantityInStockOnHand: Double = 0.0,

    @Column(name = "cost_unit_in_stock_on_hand")
    val costUnitInStockOnHand: Double = 0.0,

    @Column(name = "total_in_stock_on_hand")
    val totalInStockOnHand: Double = 0.0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var saleEntity: SaleEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var userEntity: UserEntity,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
