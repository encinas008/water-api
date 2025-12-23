package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.repository.*
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CommonService(
    val countryRepository: CountryRepository,
    val civilStatusTypeRepository: CivilStatusTypeRepository,
    val genderTypeRepository: GenderTypeRepository,
    val categoryRepository: CategoryRepository,
    val measurementRepository: MeasurementRepository,
    val saleStatusRepository: SaleStatusRepository,
    val paymentTypeRepository: PaymentTypeRepository,
    val cashFlowTypeRepository: CashFlowTypeRepository,
    val orderTypeRepository: OrderTypeRepository
) {

    fun find(): CommonOutputDto {

        val countries = mutableSetOf<CountryOutputDto>()

        countryRepository.findAllByActive(true).forEach { it ->

            val cities = mutableSetOf<CityOutputDto>()

            it.cities.forEach {
                cities.add(CityOutputDto(it.id, it.code, it.name, it.description))
            }

            countries.add(CountryOutputDto(it.id, it.code, it.name, it.description, cities))
        }

        val civilStatusTypes = mutableSetOf<CivilStatusTypeOutputDto>()
        civilStatusTypeRepository.findAllByActive(true).forEach {

            civilStatusTypes.add(CivilStatusTypeOutputDto(it.id, it.code, it.name))
        }

        val genderTypes = mutableSetOf<GenderTypeOutputDto>()
        genderTypeRepository.findAllByActive(true).forEach {

            genderTypes.add(GenderTypeOutputDto(it.id, it.code, it.name))
        }

        val categories = mutableSetOf<CategoryOutputDto>()
        categoryRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "createdAt")).forEach {

            categories.add(
                CategoryOutputDto(
                    it.id,
                    it.code,
                    it.name,
                    it.description,
                    it.createdAt,
                    it.updatedAt,
                    it.active
                )
            )
        }

        val measurements = mutableSetOf<MeasurementOutputDto>()
        measurementRepository.findAllByActive(true).forEach {
            measurements.add(
                MeasurementOutputDto(
                    id = it.id,
                    code = it.code,
                    name = it.name,
                    description = it.description,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    active = it.active
                )
            )
        }

        val saleStatusTypes = mutableSetOf<SaleStatusOutputDto>()
        saleStatusRepository.findAllByActive(true).forEach {
            saleStatusTypes.add(SaleStatusOutputDto(it.id, it.code, it.name, it.description))
        }

        val paymentTypes = mutableSetOf<PaymentTypeOutputDto>()
        paymentTypeRepository.findAllByActive(true).forEach {
            paymentTypes.add(PaymentTypeOutputDto(it.id, it.code, it.name, it.description))
        }

        val cashFlowTypes = mutableSetOf<CashFlowTypeOutputDto>()
        cashFlowTypeRepository.findAllByActive(true).forEach {
            cashFlowTypes.add(CashFlowTypeOutputDto(it.id, it.code, it.name, it.description))
        }

        val orderTypes = mutableSetOf<OrderTypeOutputDto>()
        orderTypeRepository.findAllByActive(true).forEach {
            orderTypes.add(OrderTypeOutputDto(it.id, it.code, it.name, it.description))
        }

        return CommonOutputDto(
            countries,
            civilStatusTypes,
            genderTypes,
            categories,
            measurements,
            paymentTypes,
            saleStatusTypes,
            cashFlowTypes,
            orderTypes
        )
    }
}
