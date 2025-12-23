package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.ProductEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : JpaRepository<ProductEntity, UUID> {

    fun findBySku(productCode: String): Optional<ProductEntity>

    fun findAllByActive(active: Boolean, sort: Sort): List<ProductEntity>

    fun findBySkuInAndActive(skus: List<String>, active: Boolean): List<ProductEntity>
}
