package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.repository.*
import org.springframework.stereotype.Service

@Service
class CommonService(
    val countryRepository: CountryRepository,
    val civilStatusTypeRepository: CivilStatusTypeRepository,
    val genderTypeRepository: GenderTypeRepository,
    val paymentTypeRepository: PaymentTypeRepository,
    val cashFlowTypeRepository: CashFlowTypeRepository,
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

        val paymentTypes = mutableSetOf<PaymentTypeOutputDto>()
        paymentTypeRepository.findAllByActive(true).forEach {
            paymentTypes.add(PaymentTypeOutputDto(it.id, it.code, it.name, it.description))
        }

        val cashFlowTypes = mutableSetOf<CashFlowTypeOutputDto>()
        cashFlowTypeRepository.findAllByActive(true).forEach {
            cashFlowTypes.add(CashFlowTypeOutputDto(it.id, it.code, it.name, it.description))
        }

        return CommonOutputDto(
            countries,
            civilStatusTypes,
            genderTypes,
            paymentTypes,
            cashFlowTypes,
        )
    }
}
