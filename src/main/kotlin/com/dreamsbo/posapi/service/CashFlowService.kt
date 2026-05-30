package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.dto.CashFlowInputDto
import com.dreamsbo.posapi.dto.CashFlowOutputDto
import com.dreamsbo.posapi.persistence.entity.CashFlowEntity
import com.dreamsbo.posapi.persistence.repository.*
import com.dreamsbo.posapi.util.DateUtil
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Service
class CashFlowService(
    private val cashFlowRepository: CashFlowRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
    private val cashFlowTypeRepository: CashFlowTypeRepository,
    private val cashBalanceRepository: CashBalanceRepository,
    private val boxRepository: BoxRepository,
    private val waterPaymentRepository: WaterPaymentRepository
) {

    fun findAllByUserId(
        userId: UUID,
        fromDateInMilliseconds: Long,
        toDateInMilliseconds: Long
    ): List<CashFlowOutputDto> {

        val initDateTime: OffsetDateTime = DateUtil.fromDate(fromDateInMilliseconds)
        val endDateTime: OffsetDateTime = DateUtil.toDate(toDateInMilliseconds)

        val cashFlows = mutableListOf<CashFlowOutputDto>()

        cashFlowRepository.findCashFlowsByUserAndRangeOfDates(userId, initDateTime, endDateTime).forEach {

            cashFlows.add(
                CashFlowOutputDto(
                    id = it.id,
                    box = it.cashBalance.box.name,
                    boxId = it.cashBalance.box.id,
                    assignee = "${it.cashBalance.box.user.profile.name} ${it.cashBalance.box.user.profile.lastname}",
                    type = it.cashFlowType.name,
                    description = it.description,
                    amount = it.amount,
                    active = it.active,
                    correlativeNumber = it.correlativeNumber,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            )
        }

        return cashFlows
    }

    fun findWithdrawalsByCashBalanceId(cashBalanceId: UUID): List<CashFlowOutputDto> {
        val cashFlows = cashFlowRepository.findByCashBalanceIdAndActive(cashBalanceId, true)
        
        return cashFlows
            .filter { it.cashFlowType.name == "EGRESO" }
            .sortedByDescending { it.createdAt }
            .map {
                CashFlowOutputDto(
                    id = it.id,
                    box = it.cashBalance.box.name,
                    boxId = it.cashBalance.box.id,
                    assignee = "${it.cashBalance.box.user.profile.name} ${it.cashBalance.box.user.profile.lastname}",
                    type = it.cashFlowType.name,
                    description = it.description,
                    amount = it.amount,
                    active = it.active,
                    correlativeNumber = it.correlativeNumber,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
    }

    @Transactional
    fun create(cashFlowInputDto: CashFlowInputDto): CashFlowOutputDto {

        val paymentTypeOptional = paymentTypeRepository.findById(cashFlowInputDto.paymentTypeId)
        if (paymentTypeOptional.isEmpty) {
            throw BadRequestException("Tipo de pago no encontrado por Id. PaymentTypeId = ${cashFlowInputDto.paymentTypeId}")
        }

        val cashFlowTypeOptional = cashFlowTypeRepository.findById(cashFlowInputDto.cashFlowTypeId)
        if (cashFlowTypeOptional.isEmpty) {
            throw BadRequestException("Tipo de pago no encontrado por Id. CashFlowTypeId = ${cashFlowInputDto.cashFlowTypeId}")
        }

        val cashBalance = if (cashFlowInputDto.cashBalanceId != null) {
            cashBalanceRepository.findById(cashFlowInputDto.cashBalanceId)
        } else {
            val boxOptional = boxRepository.findByUserId(cashFlowInputDto.userId, true)
            if (boxOptional.isEmpty) {
                throw BadRequestException("Caja no encontrada para el usuario. UserId = ${cashFlowInputDto.userId}")
            }
            cashBalanceRepository.findOpenBoxForUser(boxOptional.get().id)
        }

        if (cashBalance.isEmpty) {
            throw BadRequestException("No existe ninguna arqueo de caja abierto para la operación")
        }

        if (cashFlowTypeOptional.get().name == "EGRESO") {

            val cashFromSales: BigDecimal = getCurrentCashInBox(cashBalance.get().id)

            val cashFromCashFlows: BigDecimal = getCurrentCashInCashFlows(cashBalance.get().id)

            println("Dinero por ventas es: ${cashFromSales}")
            println("Dinero por movimientos es: ${cashFromCashFlows}")
            println("Monto inicial es: ${cashBalance.get().initialMoney}")

            val total = cashFromSales + cashFromCashFlows + cashBalance.get().initialMoney

            println("Total es: ${total}")

            if (cashFlowInputDto.amount > total) {
                throw BadRequestException("No existe el efectivo en cajas!")
            }
        }

        // Obtener y actualizar el correlativo del balance si existe
        var correlativeNumber: Int? = null
        val cbLinked = cashBalanceRepository.findByIdLocked(cashBalance.get().id)
            .orElseThrow { BadRequestException("No se pudo bloquear el balance de caja") }
            
        if (cashFlowTypeOptional.get().name == "EGRESO") {
            cbLinked.lastCorrelativeExpense += 1
            correlativeNumber = cbLinked.lastCorrelativeExpense
        } else {
            cbLinked.lastCorrelative += 1
            correlativeNumber = cbLinked.lastCorrelative
        }
        
        cashBalanceRepository.save(cbLinked)

        val cashFlowSaved = cashFlowRepository.save(
            CashFlowEntity(
                amount = cashFlowInputDto.amount,
                description = cashFlowInputDto.description,
                cashBalance = cbLinked,
                cashFlowType = cashFlowTypeOptional.get(),
                paymentType = paymentTypeOptional.get(),
                correlativeNumber = correlativeNumber
            )
        )

        return CashFlowOutputDto(
            id = cashFlowSaved.id,
            box = cashBalance.get().box.name,
            boxId = cashBalance.get().box.id,
            assignee = "${cashBalance.get().box.user.profile.name} ${cashBalance.get().box.user.profile.lastname}",
            type = cashFlowTypeOptional.get().name,
            description = cashFlowSaved.description,
            amount = cashFlowSaved.amount,
            active = cashFlowSaved.active,
            correlativeNumber = cashFlowSaved.correlativeNumber,
            createdAt = cashFlowSaved.createdAt,
            updatedAt = cashFlowSaved.updatedAt
        )
    }

    private fun getCurrentCashInBox(cashBalanceId: UUID): BigDecimal {

        var cashFromSales = BigDecimal(0)

        // Obtener efectivo de pagos de agua
        val waterPayments = waterPaymentRepository.findByCashBalanceId(cashBalanceId, true)
        val paymentsByType = waterPayments.groupBy { it.paymentType.name }
        var cashFromWaterPayments = BigDecimal(0)
        paymentsByType["EFECTIVO"]?.forEach {
            cashFromWaterPayments = cashFromWaterPayments.plus(it.amount)
        }

        return cashFromSales.plus(cashFromWaterPayments)
    }

    private fun getCurrentCashInCashFlows(cashBalanceId: UUID): BigDecimal {
        val cashFlows = cashFlowRepository.findByCashBalanceIdAndActive(cashBalanceId, true)

        var balance = BigDecimal.ZERO
        cashFlows.forEach {
            if (it.paymentType.name == "EFECTIVO") {
                if (it.cashFlowType.name == "INGRESO") {
                    balance = balance.add(it.amount)
                } else if (it.cashFlowType.name == "EGRESO") {
                    balance = balance.subtract(it.amount)
                }
            }
        }

        return balance
    }
}
