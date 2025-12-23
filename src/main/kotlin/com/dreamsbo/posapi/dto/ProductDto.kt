package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class ProductInputDto(
    val categoryId: UUID,
    val userId: UUID,
    val minStock: BigDecimal,
    val maxStock: BigDecimal,
    val sku: String,
    val stock: BigDecimal,
    val name: String,
    val cost: BigDecimal,
    val price: BigDecimal,
    val measurementId: UUID
)

data class ImageOutputDto(val path: String)

data class ProductOutputDto(
    val id: UUID,
    val sku: String,
    val name: String,
    val cost: BigDecimal,
    val price: BigDecimal,
    val category: CategoryOutputDto,
    val minStock: BigDecimal,
    val maxStock: BigDecimal,
    val stock: BigDecimal,
    val measurement: MeasurementOutputDto,
    val image: ImageOutputDto,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val active: Boolean
)
