package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.dto.CashFlowInputDto
import com.dreamsbo.posapi.dto.CashFlowOutputDto
import com.dreamsbo.posapi.persistence.entity.CashFlowEntity
import com.dreamsbo.posapi.persistence.repository.*
import com.dreamsbo.posapi.util.DateUtil
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
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            )
        }

        return cashFlows
    }

    fun findWithdrawalsByCashBalanceId(cashBalanceId: UUID): List<CashFlowOutputDto> {
        val cashFlows = cashFlowRepository.findByCashBalanceId(cashBalanceId)
        
        return cashFlows
            .filter { it.cashFlowType.name == "EGRESO" && it.active }
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
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
    }

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

        val cashFlowSaved = cashFlowRepository.save(
            CashFlowEntity(
                amount = cashFlowInputDto.amount,
                description = cashFlowInputDto.description,
                cashBalance = cashBalance.get(),
                cashFlowType = cashFlowTypeOptional.get(),
                paymentType = paymentTypeOptional.get()
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

        val cashFlows = cashFlowRepository.findByCashBalanceId(cashBalanceId)

        // Filtrar solo INGRESOS en efectivo (no EGRESOS)
        var cashFromCashFlows = BigDecimal(0)
        cashFlows.forEach {
            if (it.cashFlowType.name == "INGRESO" && it.paymentType.name == "EFECTIVO") {
                cashFromCashFlows = cashFromCashFlows.plus(it.amount)
            }
        }

        return cashFromCashFlows
    }
}
