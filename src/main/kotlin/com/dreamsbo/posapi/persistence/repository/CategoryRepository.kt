package com.dreamsbo.posapi.persistence.repository

import com.dreamsbo.posapi.persistence.entity.CategoryEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<CategoryEntity, UUID> {

    fun findAllByActive(active: Boolean, sort: Sort): MutableList<CategoryEntity>
}
