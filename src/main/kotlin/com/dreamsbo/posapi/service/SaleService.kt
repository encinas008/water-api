package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.ProductEntity
import com.dreamsbo.posapi.persistence.entity.SaleDetailEntity
import com.dreamsbo.posapi.persistence.entity.SaleEntity
import com.dreamsbo.posapi.persistence.repository.*
import com.dreamsbo.posapi.util.DateUtil
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.*
import java.util.*


@Service
class SaleService(
    private val salesStatusTypeRepository: SaleStatusRepository,
    private val saleRepository: SaleRepository,
    private val saleDetailRepository: SaleDetailRepository,
    private val partnerRepository: PartnerRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val boxRepository: BoxRepository,
    private val cashBalanceRepository: CashBalanceRepository,
    private val orderTypeRepository: OrderTypeRepository,
) {

    @Transactional
    fun create(saleInputDto: SaleInputDto): SaleOutputDto {

        checkOpenBoxForUser(saleInputDto.userId)

        val salesStatusTypeEntity = salesStatusTypeRepository.findByNameAndActive(saleInputDto.salesStatusName, true)
        if (salesStatusTypeEntity.isEmpty) {

            throw NotFoundEntityException("No se ha encontrado el estado de venta. EstadoDeVenta = ${saleInputDto.salesStatusName}")
        }

        val partnerEntity = partnerRepository.findById(saleInputDto.clientId)
        if (partnerEntity.isEmpty) {

            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = ${saleInputDto.clientId}")
        }

        val userEntity = userRepository.findById(saleInputDto.userId)
        if (userEntity.isEmpty) {

            throw NotFoundEntityException("No se ha encontrado el usuario. UserId = $saleInputDto.userId")
        }

        val paymentTypeEntity = paymentTypeRepository.findById(saleInputDto.paymentTypeId)
        if (paymentTypeEntity.isEmpty) {

            throw NotFoundEntityException("No se ha encontrado el tipo de pago. PaymentTypeId = ${saleInputDto.paymentTypeId}")
        }

        val orderTypeEntity = orderTypeRepository.findById(saleInputDto.orderTypeId)
        if (orderTypeEntity.isEmpty) {

            throw NotFoundEntityException("No se ha encontrado el tipo de order. OrderTypeId = ${saleInputDto.orderTypeId}")
        }

        val boxOptional = boxRepository.findByUserId(saleInputDto.userId, true)

        val cashBalanceOptional = cashBalanceRepository.findOpenBoxForUser(boxOptional.get().id)

        val savedSale = saleRepository.save(
            SaleEntity(
                saleStatus = salesStatusTypeEntity.get(),
                paymentType = paymentTypeEntity.get(),
                partner = partnerEntity.get(),
                user = userEntity.get(),
                cashBalance = cashBalanceOptional.get(),
                subTotal = saleInputDto.subTotal,
                discount = saleInputDto.discount,
                moneyToBack = saleInputDto.moneyToBack,
                total = saleInputDto.total,
                quantityOfProducts = saleInputDto.quantityOfProducts,
                orderType = orderTypeEntity.get()
            )
        )

        val saleDetails: MutableList<SaleDetailEntity> = mutableListOf()

        saleInputDto.items.forEach {
            saleDetails.add(
                SaleDetailEntity(
                    sale = savedSale,
                    sku = it.sku,
                    name = it.name,
                    quantity = it.quantity,
                    price = it.price,
                    subTotal = it.quantity * it.price,
                    category = it.category,
                )
            )
        }

        saleDetailRepository.saveAll(saleDetails)

        val skus = saleInputDto.items.map { it.sku }.toList()
        reduceQuantitiesOnProducts(skus, saleInputDto.items)

        return SaleOutputDto(savedSale.id)
    }

    private fun checkOpenBoxForUser(userId: UUID) {

        val boxOptional = boxRepository.findByUserId(userId, true)
        if (boxOptional.isEmpty) {
            throw BadRequestException("No se ha encontrado ninguna caja para el usuario.")
        }

        val cashBalanceOptional = cashBalanceRepository.findOpenBoxForUser(boxOptional.get().id)
        if (cashBalanceOptional.isEmpty) {
            throw BadRequestException("No se ha encontrado ninguna apertura de arqueo de caja.")
        }
    }

    private fun reduceQuantitiesOnProducts(skus: List<String>, items: MutableList<ItemDto>) {
        val products = productRepository.findBySkuInAndActive(skus, true)

        val productMap: MutableMap<String, ProductEntity> = mutableMapOf()
        products.forEach {
            productMap[it.sku] = it
        }

        items.forEach {
            val product = productMap[it.sku]
            if (it.quantity <= product?.stock) {
                product?.stock = product?.stock?.minus(it.quantity)!!
            }
        }

        productRepository.saveAll(products);
    }

    fun getAllByUser(
        userId: UUID,
        fromDateInMilliseconds: Long,
        toDateInMilliseconds: Long
    ): List<SaleDetailOutputDto> {
        val initDateTime: OffsetDateTime = DateUtil.fromDate(fromDateInMilliseconds)
        val endDateTime: OffsetDateTime = DateUtil.toDate(toDateInMilliseconds)

        val sales = saleRepository.findSalesByUserAndRangeOfDates(userId, initDateTime, endDateTime)

        val saleDetails: MutableList<SaleDetailOutputDto> = mutableListOf()

        sales.forEach { it ->
            val items: MutableList<ItemOutputDto> = mutableListOf()
            it.saleDetails.forEach {
                items.add(
                    ItemOutputDto(
                        it.sku,
                        it.name,
                        it.category,
                        it.category.substring(0, 2),
                        it.quantity,
                        it.price,
                        it.subTotal
                    )
                )
            }
            saleDetails.add(toSaleDetailsOutput(it, items))
        }

        return saleDetails
    }

    fun getSaleById(saleId: UUID): SaleDetailOutputDto {

        val saleOptional = saleRepository.findById(saleId)

        if (saleOptional.isEmpty) {
            throw BadRequestException("No se ha encontrado la venta!")
        }

        val saleEntity = saleOptional.get()

        val details = saleDetailRepository.findBySaleId(saleEntity.id)

        val productDetails: MutableList<ItemOutputDto> = mutableListOf()

        details.forEach {
            productDetails.add(
                ItemOutputDto(
                    sku = it.sku,
                    name = it.name,
                    category = it.category,
                    shortCategoryName = it.category.substring(0, 2),
                    quantity = it.quantity,
                    price = it.price,
                    subTotal = it.subTotal,
                )
            )
        }

        return toSaleDetailsOutput(saleEntity, productDetails)
    }

    private fun toSaleDetailsOutput(
        saleEntity: SaleEntity,
        productDetails: MutableList<ItemOutputDto>
    ): SaleDetailOutputDto {
        return SaleDetailOutputDto(
            userName = "${saleEntity.user.profile.name} ${saleEntity.user.profile.lastname}",
            clientName = saleEntity.partner.fullName,
            paymentTypeName = saleEntity.paymentType.name,
            salesStatus = saleEntity.saleStatus.name,
            quantityOfProducts = saleEntity.quantityOfProducts,
            discount = saleEntity.discount,
            moneyToBack = saleEntity.moneyToBack,
            subTotal = saleEntity.subTotal,
            total = saleEntity.total,
            createdAt = saleEntity.createdAt,
            updatedAt = saleEntity.updatedAt,
            products = productDetails,
            orderNumber = saleEntity.orderNumber,
            orderFor = saleEntity.orderType.name
        )
    }
}
