package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class SaleInputDto(
    val clientId: UUID, // Keeping clientId for backward compatibility (references partnerId)
    val paymentTypeId: UUID,
    val userId: UUID,
    val salesStatusName: String,
    val items: MutableList<ItemDto>,
    val subTotal: BigDecimal,
    val discount: BigDecimal = BigDecimal(0),
    val moneyToBack: BigDecimal = BigDecimal(0),
    val total: BigDecimal,
    val quantityOfProducts: Int,
    val orderTypeId: UUID
)

data class ItemDto(
    val sku: String,
    val name: String,
    val category: String,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val subTotal: BigDecimal
)

data class ItemOutputDto(
    val sku: String,
    val name: String,
    val category: String,
    val shortCategoryName: String,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val subTotal: BigDecimal
)

data class SaleOutputDto(
    val saleId: UUID
)

data class SaleDetailOutputDto(
    val userName: String,
    val clientName: String,
    val paymentTypeName: String,
    val quantityOfProducts: Int,
    val discount: BigDecimal,
    val moneyToBack: BigDecimal,
    val subTotal: BigDecimal,
    val total: BigDecimal,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val products: List<ItemOutputDto>?,
    val orderNumber: BigDecimal?,
)
