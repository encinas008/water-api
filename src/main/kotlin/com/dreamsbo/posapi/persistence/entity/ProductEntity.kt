package com.dreamsbo.posapi.persistence.entity

import com.dreamsbo.core.annotation.NoArg
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(schema = "pos", name = "product")
@NoArg
data class ProductEntity(
    @Id
    @Column(name = "product_id")
    var id: UUID = UUID.randomUUID(),

    var sku: String,

    @Column(name = "min_stock")
    var minStock: BigDecimal,

    @Column(name = "max_stock")
    var maxStock: BigDecimal,

    var stock: BigDecimal,
    var name: String,
    var price: BigDecimal,
    var cost: BigDecimal ,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var category: CategoryEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "measurement_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var measurement: MeasurementEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var image: ImageEntity? = null,

    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    var updatedAt: OffsetDateTime? = null,
    var active: Boolean = true,
)
