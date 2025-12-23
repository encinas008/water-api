package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CityEntity
import com.dreamsbo.posapi.persistence.entity.CountryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CityRepository : JpaRepository<CityEntity, UUID> {

    fun findAllByCountryIdAndActive(countryId: UUID, active: Boolean): MutableSet<CityEntity>
}
