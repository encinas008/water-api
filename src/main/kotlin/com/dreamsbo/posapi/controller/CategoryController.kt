package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.CategoryOutputDto
import com.dreamsbo.posapi.service.CategoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/categories")
class CategoryController(
    val categoryService: CategoryService,
) {

    @GetMapping
    fun getAll(): List<CategoryOutputDto> {

        return categoryService.findAll()
    }
}
