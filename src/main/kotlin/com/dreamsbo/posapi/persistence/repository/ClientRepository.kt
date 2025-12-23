package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.ClientEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClientRepository : JpaRepository<ClientEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<ClientEntity>
}
