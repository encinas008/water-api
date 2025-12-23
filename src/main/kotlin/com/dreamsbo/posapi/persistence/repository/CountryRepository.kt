package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CountryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CountryRepository : JpaRepository<CountryEntity, UUID> {

    fun findAllByActive(active: Boolean): MutableSet<CountryEntity>

    fun findByName(name: String): CountryEntity
}
