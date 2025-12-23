package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.ConflictException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.ProductEntity
import com.dreamsbo.posapi.persistence.repository.CategoryRepository
import com.dreamsbo.posapi.persistence.repository.MeasurementRepository
import com.dreamsbo.posapi.persistence.repository.ProductRepository
import com.dreamsbo.posapi.persistence.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductService(
    private val categoryRepository: CategoryRepository,
    private val measurementRepository: MeasurementRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun create(productInputDto: ProductInputDto): ProductOutputDto {

        val productEntityByCode = productRepository.findBySku(productInputDto.sku)
        if (!productEntityByCode.isEmpty) {

            throw ConflictException("Ya existe un producto con este codigo. Codigo = ${productInputDto.sku}")
        }

        val userEntity = userRepository.findById(productInputDto.userId)
        if (userEntity.isEmpty) {

            throw NotFoundEntityException("No se ha encontrado el user. UserId = $productInputDto.userId")
        }

        val categoryEntity = categoryRepository.findById(productInputDto.categoryId)
        if (categoryEntity.isEmpty) {

            throw BadRequestException("La categoria no ha sido encontrado. CategoryId = ${productInputDto.categoryId}")
        }

        val measurementEntity = measurementRepository.findById(productInputDto.measurementId)
        if (measurementEntity.isEmpty) {

            throw BadRequestException("La unidad de medida no ha sido encontrado. MeasurementId = ${productInputDto.measurementId}")
        }

        val savedProduct = productRepository.save(
            ProductEntity(
                sku = productInputDto.sku,
                name = productInputDto.name,
                price = productInputDto.price,
                cost = productInputDto.cost,
                minStock = productInputDto.minStock,
                maxStock = productInputDto.maxStock,
                category = categoryEntity.get(),
                measurement = measurementEntity.get(),
                stock = productInputDto.stock,
            )
        )

        return toProductOutputDto(savedProduct)
    }

    fun findAll(): List<ProductOutputDto> {

        val products = mutableListOf<ProductOutputDto>()

        productRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "sku")).forEach {

            products.add(toProductOutputDto(it))
        }

        return products
    }

    fun findById(productId: UUID): ProductOutputDto {

        val productOptional = productRepository.findById(productId)
        if (productOptional.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el producto. productId = $productId.")
        }

        val productEntity = productOptional.get()

        return toProductOutputDto(productEntity)
    }

    private fun toProductOutputDto(savedProduct: ProductEntity): ProductOutputDto {
        return ProductOutputDto(
            id = savedProduct.id,
            sku = savedProduct.sku,
            name = savedProduct.name,
            price = savedProduct.price,
            cost = savedProduct.cost,
            category = CategoryOutputDto(
                id = savedProduct.category.id,
                code = savedProduct.category.code,
                name = savedProduct.category.name,
                description = savedProduct.category.description,
                createdAt = savedProduct.category.createdAt,
                updatedAt = savedProduct.category.updatedAt,
                active = savedProduct.category.active
            ),
            minStock = savedProduct.minStock,
            maxStock = savedProduct.maxStock,
            stock = savedProduct.stock,
            measurement = MeasurementOutputDto(
                id = savedProduct.measurement.id,
                code = savedProduct.measurement.code,
                name = savedProduct.measurement.name,
                description = savedProduct.measurement.description,
                createdAt = savedProduct.measurement.createdAt,
                updatedAt = savedProduct.measurement.updatedAt,
                active = savedProduct.measurement.active
            ),
            image = ImageOutputDto(savedProduct.image?.url ?: "/assets/images/product/default.png"),
            createdAt = savedProduct.createdAt,
            updatedAt = savedProduct.updatedAt,
            active = savedProduct.active
        )
    }

    fun update(productId: UUID, productInputDto: ProductInputDto): ProductOutputDto {

        val userEntity = userRepository.findById(productInputDto.userId)
        if (userEntity.isEmpty) {

            throw NotFoundEntityException("No se ha encontrado el user. UserId = $productInputDto.userId")
        }

        val categoryEntity = categoryRepository.findById(productInputDto.categoryId)
        if (categoryEntity.isEmpty) {

            throw BadRequestException("La categoria no ha sido encontrado. CategoryId = ${productInputDto.categoryId}")
        }

        val measurementEntity = measurementRepository.findById(productInputDto.measurementId)
        if (measurementEntity.isEmpty) {

            throw BadRequestException("La unidad de medida no ha sido encontrado. MeasurementId = ${productInputDto.measurementId}")
        }

        val productEntity = productRepository.findById(productId)
        if (productEntity.isEmpty) {

            throw BadRequestException("El producto no existe. ProductId = ${productInputDto.measurementId}")
        }
        val product = productEntity.get()
        product.sku = productInputDto.sku
        product.name = productInputDto.name
        product.price = productInputDto.price
        product.cost = productInputDto.cost
        product.minStock = productInputDto.minStock
        product.maxStock = productInputDto.maxStock
        product.category = categoryEntity.get()
        product.measurement = measurementEntity.get()
        product.stock = productInputDto.stock

        productRepository.save(product)

        return toProductOutputDto(product)
    }
}
