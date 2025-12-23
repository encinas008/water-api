package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.ProductInputDto
import com.dreamsbo.posapi.dto.ProductOutputDto
import com.dreamsbo.posapi.service.ProductService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/products")
class ProductController(
    val productService: ProductService,
) {

    @PostMapping
    fun create(@RequestBody productInputDto: ProductInputDto): ProductOutputDto {

        return productService.create(productInputDto)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable("id") productId: UUID, @RequestBody productInputDto: ProductInputDto): ProductOutputDto {

        return productService.update(productId, productInputDto)
    }

    @GetMapping
    fun getAll(): List<ProductOutputDto> {

        return productService.findAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") productId: UUID): ProductOutputDto {

        return productService.findById(productId)
    }
}
