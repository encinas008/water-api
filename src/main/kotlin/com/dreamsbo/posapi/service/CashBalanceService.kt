package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.CashBalanceEntity
import com.dreamsbo.posapi.persistence.repository.BoxRepository
import com.dreamsbo.posapi.persistence.repository.CashBalanceRepository
import com.dreamsbo.posapi.persistence.repository.CashFlowRepository
import com.dreamsbo.posapi.persistence.repository.WaterPaymentRepository
import com.dreamsbo.posapi.util.DateUtil
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Service
class CashBalanceService(
    private val boxRepository: BoxRepository,
    private val cashBalanceRepository: CashBalanceRepository,
    private val waterPaymentRepository: WaterPaymentRepository,
    private val cashFlowRepository: CashFlowRepository,
    @PersistenceContext private val entityManager: EntityManager
) {

    fun findAllByUserId(
        userId: UUID,
        fromDateInMilliseconds: Long,
        toDateInMilliseconds: Long
    ): List<CashBalanceOutputDto> {

        val initDateTime: OffsetDateTime = DateUtil.fromDate(fromDateInMilliseconds)
        val endDateTime: OffsetDateTime = DateUtil.toDate(toDateInMilliseconds)

        val cashBalances = mutableListOf<CashBalanceOutputDto>()

        cashBalanceRepository.findCashBalancesByUserAndRangeOfDates(userId, initDateTime, endDateTime).forEach {

            cashBalances.add(
                CashBalanceOutputDto(
                    id = it.id,
                    description = it.description,
                    assignee = "${it.box.user.profile.name} ${it.box.user.profile.lastname}",
                    openTime = it.openTime,
                    closeTime = it.closeTime,
                    initialMoney = it.initialMoney,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    active = it.active,
                )
            )
        }

        return cashBalances
    }

    fun create(cashBalanceInputDto: CashBalanceInputDto): CashBalanceOutputDto {

        entityManager.createNativeQuery("SELECT SETVAL(?, ?, true)")
            .setParameter(1, "pos.order_number_seq")
            .setParameter(2, 1).singleResult

        val boxOptional = boxRepository.findByUserId(cashBalanceInputDto.userId, true)
        if (boxOptional.isEmpty) {
            throw BadRequestException("Ninguna caja aperturada para el usuario. UserId = ${cashBalanceInputDto.userId}")
        }

        val cashBalances = cashBalanceRepository.findOpenBoxForUser(boxOptional.get().id)
        if (!cashBalances.isEmpty) {
            throw BadRequestException("Tienes que cerrar tu arqueo para aperturar uno nuevo!")
        }

        val cashBalanceSaved = cashBalanceRepository.save(
            CashBalanceEntity(
                openTime = OffsetDateTime.now(),
                initialMoney = cashBalanceInputDto.moneyToOpenCashBalance,
                box = boxOptional.get(),
            )
        )

        return CashBalanceOutputDto(
            id = cashBalanceSaved.id,
            description = cashBalanceSaved.description,
            assignee = "${cashBalanceSaved.box.user.profile.name} ${cashBalanceSaved.box.user.profile.lastname}",
            openTime = cashBalanceSaved.openTime,
            closeTime = cashBalanceSaved.closeTime,
            initialMoney = cashBalanceSaved.initialMoney,
            createdAt = cashBalanceSaved.createdAt,
            updatedAt = cashBalanceSaved.updatedAt,
            active = cashBalanceSaved.active,
        )
    }

    fun close(closeCashBalanceInputDto: CloseCashBalanceInputDto): Boolean {

        val cashBalanceOptional = cashBalanceRepository.findById(closeCashBalanceInputDto.cashBalanceId)
        if (cashBalanceOptional.isEmpty) {
            throw BadRequestException("No existe ningun arqueo aperturada para el usuario.")
        }

        val cashBalanceEntity = cashBalanceOptional.get()
        cashBalanceEntity.closeTime = OffsetDateTime.now()
        cashBalanceEntity.updatedAt = OffsetDateTime.now()
        cashBalanceEntity.cashInBox = getCashBalanceInBox(cashBalanceOptional.get().id)

        cashBalanceRepository.save(cashBalanceEntity)

        return true
    }

    private fun getCashBalanceInBox(cashBalanceId: UUID): BigDecimal {
        val payments = waterPaymentRepository.findByCashBalanceId(cashBalanceId, true)

        val paymentsByType = payments.groupBy { it.paymentType.name }

        var cashFromPayments = BigDecimal(0)
        paymentsByType["EFECTIVO"]?.forEach {
            cashFromPayments = cashFromPayments.plus(it.amount)
        }

        return cashFromPayments
    }

    fun getDetails(cashBalanceId: UUID): CashBalanceDetailsOutputDto {

        val cashBalanceOptional = cashBalanceRepository.findById(cashBalanceId)
        if (cashBalanceOptional.isEmpty) {
            throw BadRequestException("No existe ningun arqueo aperturada para el usuario.")
        }

        val cashBalanceEntity = cashBalanceOptional.get()

        val cashFromSalesDetails = cashFromSales(cashBalanceEntity.id)
        val cashFromCashFlowsDetails = cashFromCashFlows(cashBalanceEntity.id)
        val cashBalanceDetails = getCashBalanceDetails(
            cashFromSalesDetails,
            cashFromCashFlowsDetails,
            cashBalanceOptional.get().initialMoney
        )

        return CashBalanceDetailsOutputDto(
            id = cashBalanceEntity.id,
            description = cashBalanceEntity.description,
            assignee = "${cashBalanceEntity.box.user.profile.name} ${cashBalanceEntity.box.user.profile.lastname}",
            boxName = cashBalanceEntity.box.name,
            openTime = cashBalanceEntity.openTime,
            closeTime = cashBalanceEntity.closeTime,
            initialMoney = cashBalanceEntity.initialMoney,
            cashFromSalesDetails = cashFromSalesDetails,
            cashFromCashFlowsDetails = cashFromCashFlowsDetails,
            cashBalanceDetails = cashBalanceDetails,
            active = cashBalanceEntity.active,
            createdAt = cashBalanceEntity.createdAt,
            updatedAt = cashBalanceEntity.updatedAt
        )
    }

    private fun cashFromSales(cashBalanceId: UUID): CashFromSaleDetails {
        val payments = waterPaymentRepository.findByCashBalanceId(cashBalanceId, true)

        val paymentsByType = payments.groupBy { it.paymentType.name }

        var cashFromPayments = BigDecimal(0)
        paymentsByType["EFECTIVO"]?.forEach {
            cashFromPayments = cashFromPayments.plus(it.amount)
        }

        var cashFromQr = BigDecimal(0)
        paymentsByType["QR"]?.forEach {
            cashFromQr = cashFromQr.plus(it.amount)
        }

        var cashFromTransfer = BigDecimal(0)
        paymentsByType["TRANSFERENCIA"]?.forEach {
            cashFromTransfer = cashFromTransfer.plus(it.amount)
        }

        return CashFromSaleDetails(
            cashFromPayments,
            cashFromQr,
            cashFromTransfer
        )
    }

    private fun cashFromCashFlows(cashBalanceId: UUID): CashFromCashFlowsDetails {

        val cashFlows = cashFlowRepository.findByCashBalanceId(cashBalanceId)

        val cashByCashFlowType = cashFlows.groupBy { it.cashFlowType.name }

        var cashIn = BigDecimal(0)
        var cashQrIn = BigDecimal(0)
        var cashTransferIn = BigDecimal(0)
        cashByCashFlowType["INGRESO"]?.forEach {

            if (it.paymentType.name == "EFECTIVO") {
                cashIn = cashIn.plus(it.amount)
            }

            if (it.paymentType.name == "QR") {
                cashQrIn = cashQrIn.plus(it.amount)
            }

            if (it.paymentType.name == "TRANSFERENCIA") {
                cashTransferIn = cashTransferIn.plus(it.amount)
            }
        }


        var cashOut = BigDecimal(0)
        cashByCashFlowType["EGRESO"]?.forEach {
            cashOut = cashOut.plus(it.amount)
        }

        return CashFromCashFlowsDetails(
            cashIn = cashIn,
            cashQrIn = cashQrIn,
            cashTransferIn = cashTransferIn,
            cashOut = cashOut
        )
    }

    private fun getCashBalanceDetails(
        cashFromSales: CashFromSaleDetails,
        cashFromCashFlows: CashFromCashFlowsDetails,
        initialMoney: BigDecimal
    ): CashBalanceDetails {

        return CashBalanceDetails(
            totalCashFromSales = cashFromSales.cash.plus(cashFromSales.qr).plus(cashFromSales.transference),
            cashFromSalesInCash = cashFromSales.cash,
            cashFromSalesInOthers = cashFromSales.qr.plus(cashFromSales.transference),
            totalCash = cashFromSales.cash.plus(cashFromCashFlows.cashIn),
            totalOthers = cashFromSales.qr.plus(cashFromSales.transference).plus(cashFromCashFlows.cashQrIn)
                .plus(cashFromCashFlows.cashTransferIn),
            totalCashInBox = cashFromSales.cash.plus(cashFromCashFlows.cashIn).minus(cashFromCashFlows.cashOut)
                .plus(initialMoney),
        )
    }

    fun findLastCashBalanceByUser(userId: UUID): List<CashBalanceOutputDto> {

        val cashBalances = mutableListOf<CashBalanceOutputDto>()

        cashBalanceRepository.findLastCashBalanceOpenForUser(userId).forEach {

            cashBalances.add(
                CashBalanceOutputDto(
                    id = it.id,
                    description = it.description,
                    assignee = "${it.box.user.profile.name} ${it.box.user.profile.lastname}",
                    openTime = it.openTime,
                    closeTime = it.closeTime,
                    initialMoney = it.initialMoney,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    active = it.active,
                )
            )
        }

        return cashBalances
    }

    fun findAllActiveCashBalances(): List<CashBalanceOutputDto> {
        val cashBalances = mutableListOf<CashBalanceOutputDto>()

        cashBalanceRepository.findAllActiveCashBalances().forEach {
            cashBalances.add(
                CashBalanceOutputDto(
                    id = it.id,
                    description = it.description,
                    assignee = "${it.box.user.profile.name} ${it.box.user.profile.lastname}",
                    openTime = it.openTime,
                    closeTime = it.closeTime,
                    initialMoney = it.initialMoney,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    active = it.active,
                )
            )
        }

        return cashBalances
    }
}
