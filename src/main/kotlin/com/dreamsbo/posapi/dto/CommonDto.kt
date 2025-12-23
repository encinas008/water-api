package com.dreamsbo.posapi.dto

import org.hibernate.query.Order
import java.time.OffsetDateTime
import java.util.*

data class CityOutputDto(val id: UUID, val code: String, val name: String, val description: String)

data class CountryOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,
    val cities: MutableSet<CityOutputDto>,
)

data class CivilStatusTypeOutputDto(val id: UUID, val code: String, val name: String)

data class GenderTypeOutputDto(val id: UUID, val code: String, val name: String)

data class CategoryOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,

    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val active: Boolean,
)

data class MeasurementOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,

    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val active: Boolean,
)

data class SaleStatusOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,
)

data class PaymentTypeOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,
)

data class CashFlowTypeOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,
)

data class OrderTypeOutputDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,
)

data class CommonOutputDto(
    val countries: MutableSet<CountryOutputDto>,
    val civilStatusTypes: MutableSet<CivilStatusTypeOutputDto>,
    val genderTypes: MutableSet<GenderTypeOutputDto>,
    val categories: MutableSet<CategoryOutputDto>,
    val measurements: MutableSet<MeasurementOutputDto>,
    val paymentTypes: MutableSet<PaymentTypeOutputDto>,
    val saleStatus: MutableSet<SaleStatusOutputDto>,
    val cashFlowTypes: MutableSet<CashFlowTypeOutputDto>,
    val orderTypes: MutableSet<OrderTypeOutputDto>,
)
