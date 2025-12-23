package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.CategoryOutputDto
import com.dreamsbo.posapi.persistence.repository.CategoryRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {

    fun findAll(): List<CategoryOutputDto> {

        val categories = mutableListOf<CategoryOutputDto>()

        categoryRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt")).forEach {

            categories.add(
                CategoryOutputDto(
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

        return categories
    }
}
