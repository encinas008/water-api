package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "sale_detail")
@NoArg
data class SaleDetailEntity(
    @Id
    @Column(name = "sale_detail_id")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var sale: SaleEntity,

    var sku: String,
    var name: String,
    var quantity: BigDecimal,
    var price: BigDecimal,
    var subTotal: BigDecimal,
    var category: String,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
