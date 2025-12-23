package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.BillConceptItemDto
import com.dreamsbo.posapi.dto.PaymentReceiptDto
import com.dreamsbo.posapi.dto.PaymentReceiptFullDto
import com.dreamsbo.posapi.dto.WaterPaymentInputDto
import com.dreamsbo.posapi.dto.WaterPaymentOutputDto
import com.dreamsbo.posapi.persistence.entity.CashFlowEntity
import com.dreamsbo.posapi.persistence.entity.WaterPaymentEntity
import com.dreamsbo.posapi.persistence.repository.*
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class WaterPaymentService(
    private val waterPaymentRepository: WaterPaymentRepository,
    private val waterBillRepository: WaterBillRepository,
    private val partnerRepository: PartnerRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
    private val cashBalanceRepository: CashBalanceRepository,
    private val userRepository: UserRepository,
    private val billStatusTypeRepository: BillStatusTypeRepository,
    private val billConceptItemRepository: BillConceptItemRepository,
    private val cashFlowRepository: CashFlowRepository,
    private val cashFlowTypeRepository: CashFlowTypeRepository,
) {

    @Transactional
    fun recordPayment(input: WaterPaymentInputDto): WaterPaymentOutputDto {
        val bill = waterBillRepository.findById(input.waterBillId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la factura. BillId = ${input.waterBillId}") }

        val partner = partnerRepository.findById(input.partnerId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el socio. PartnerId = ${input.partnerId}") }

        val paymentType = paymentTypeRepository.findById(input.paymentTypeId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el tipo de pago. PaymentTypeId = ${input.paymentTypeId}") }

        val user = userRepository.findById(input.userId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el usuario. UserId = $input.userId") }

        val cashBalanceOptional = input.cashBalanceId?.let {
            cashBalanceRepository.findById(it)
        }

        // Validate payment amount
        if (input.amount <= BigDecimal.ZERO) {
            throw BadRequestException("El monto del pago debe ser mayor a cero")
        }

        if (input.amount > bill.remainingBalance) {
            throw BadRequestException("El monto del pago (${input.amount}) no puede ser mayor al saldo pendiente (${bill.remainingBalance})")
        }

        val receiptNumber = generateReceiptNumber(partner.id)

        val payment = WaterPaymentEntity(
            waterBill = bill,
            partner = partner,
            paymentDate = input.paymentDate,
            amount = input.amount,
            paymentType = paymentType,
            cashBalance = cashBalanceOptional?.orElse(null),
            user = user,
            receiptNumber = receiptNumber,
            observation = input.observation
        )

        val savedPayment = waterPaymentRepository.save(payment)

        // Update bill amounts and status
        applyPaymentToBill(bill, input.amount)

        // Update partner debt
        partner.currentDebt = partner.currentDebt.subtract(input.amount)
        partnerRepository.save(partner)

        // Crear CashFlow automáticamente si hay cashBalanceId
        if (cashBalanceOptional != null && cashBalanceOptional.isPresent) {
            try {
                createCashFlowFromPayment(savedPayment, cashBalanceOptional.get())
            } catch (e: Exception) {
                // Log el error pero no fallar el pago si el cashFlow no se puede crear
                println("⚠️ Error al crear CashFlow para el pago ${savedPayment.id}: ${e.message}")
                e.printStackTrace()
            }
        }

        return toWaterPaymentOutputDto(savedPayment)
    }

    fun getPaymentHistory(partnerId: UUID): List<WaterPaymentOutputDto> {
        val payments = waterPaymentRepository.findByPartnerIdAndActive(
            partnerId,
            true,
            Sort.by(Sort.Direction.DESC, "paymentDate")
        )
        return payments.map { toWaterPaymentOutputDto(it) }
    }

    fun getPaymentById(id: UUID): WaterPaymentOutputDto {
        val payment = waterPaymentRepository.findById(id)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el pago. PaymentId = $id") }
        return toWaterPaymentOutputDto(payment)
    }

    fun generateReceipt(paymentId: UUID): PaymentReceiptDto {
        val payment = waterPaymentRepository.findById(paymentId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el pago. PaymentId = $paymentId") }

        val bill = payment.waterBill
        val partner = payment.partner

        val formatter = DateTimeFormatter.ofPattern("MM/yyyy")
        val billingPeriod = "${bill.billingPeriodStart.format(formatter)} - ${bill.billingPeriodEnd.format(formatter)}"

        val previousBalance = bill.remainingBalance.add(payment.amount)
        val newBalance = bill.remainingBalance

        return PaymentReceiptDto(
            receiptNumber = payment.receiptNumber,
            paymentDate = payment.paymentDate,
            partnerName = partner.fullName,
            partnerIdentificationNumber = partner.partnerIdentificationNumber,
            waterConnectionNumber = partner.waterConnectionNumber,
            billNumber = bill.billNumber,
            billingPeriod = billingPeriod,
            amount = payment.amount,
            paymentTypeName = payment.paymentType.name,
            cashierName = "${payment.user.profile.name} ${payment.user.profile.lastname}",
            previousBalance = previousBalance,
            newBalance = newBalance
        )
    }

    fun generateFullReceipt(paymentId: UUID, receiptType: String = "NOTA DE PAGO"): PaymentReceiptFullDto {
        val payment = waterPaymentRepository.findById(paymentId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el pago. PaymentId = $paymentId") }

        val bill = payment.waterBill
        val partner = payment.partner
        val reading = bill.reading

        // Obtener conceptos de la factura
        val concepts = billConceptItemRepository.findByWaterBillIdAndActive(bill.id, true)
            .map { concept ->
                BillConceptItemDto(
                    id = concept.id,
                    conceptName = concept.conceptName,
                    assignedDate = concept.assignedDate,
                    amount = concept.amount
                )
            }

        // Formatear fecha y hora de pago
        val now = java.time.OffsetDateTime.now(java.time.ZoneOffset.ofHours(-4))
        val paymentDateFormatted = String.format(
            "%02d:%02d %02d-%s-%d",
            now.hour,
            now.minute,
            payment.paymentDate.dayOfMonth,
            getMonthName(payment.paymentDate.monthValue),
            payment.paymentDate.year
        )

        // Obtener mes de pago del período de facturación
        val paymentMonth = getMonthName(bill.billingPeriodStart.monthValue).uppercase()
        val paymentMonthDate = String.format(
            "%02d-%s-%d",
            bill.billingPeriodEnd.dayOfMonth,
            getMonthName(bill.billingPeriodEnd.monthValue),
            bill.billingPeriodEnd.year
        )

        // Lecturas del medidor
        val currentReading = reading?.currentReading ?: BigDecimal.ZERO
        val previousReading = reading?.previousReading ?: BigDecimal.ZERO
        val consumption = reading?.consumption ?: BigDecimal.ZERO

        // Convertir total a palabras
        val totalInWords = numberToWords(bill.totalAmount)

        return PaymentReceiptFullDto(
            receiptNumber = payment.receiptNumber,
            receiptType = receiptType,
            partnerNumber = partner.waterConnectionNumber?.substringAfterLast("-") ?: "",
            partnerName = partner.fullName,
            partnerIdentificationNumber = partner.partnerIdentificationNumber,
            paymentDate = paymentDateFormatted,
            currentReading = currentReading,
            previousReading = previousReading,
            consumptionM3 = consumption,
            meterNumber = partner.waterMeterNumber ?: "0",
            paymentMonth = paymentMonth,
            paymentMonthCode = null,
            paymentMonthDate = paymentMonthDate,
            concepts = concepts,
            totalAmount = bill.totalAmount,
            totalAmountInWords = totalInWords
        )
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "enero"
            2 -> "febrero"
            3 -> "marzo"
            4 -> "abril"
            5 -> "mayo"
            6 -> "junio"
            7 -> "julio"
            8 -> "agosto"
            9 -> "septiembre"
            10 -> "octubre"
            11 -> "noviembre"
            12 -> "diciembre"
            else -> ""
        }
    }

    private fun numberToWords(amount: BigDecimal): String {
        val wholePart = amount.toInt()
        val cents = (amount.remainder(BigDecimal.ONE) * BigDecimal(100)).toInt()
        
        val wholeWords = convertNumberToWords(wholePart)
        val centsWords = if (cents > 0) {
            " con ${convertNumberToWords(cents)} centavos"
        } else {
            ""
        }
        
        return "Son $wholeWords Bolivianos$centsWords."
    }

    private fun convertNumberToWords(number: Int): String {
        if (number == 0) return "Cero"
        if (number < 20) {
            return when (number) {
                1 -> "Uno"
                2 -> "Dos"
                3 -> "Tres"
                4 -> "Cuatro"
                5 -> "Cinco"
                6 -> "Seis"
                7 -> "Siete"
                8 -> "Ocho"
                9 -> "Nueve"
                10 -> "Diez"
                11 -> "Once"
                12 -> "Doce"
                13 -> "Trece"
                14 -> "Catorce"
                15 -> "Quince"
                16 -> "Dieciséis"
                17 -> "Diecisiete"
                18 -> "Dieciocho"
                19 -> "Diecinueve"
                else -> ""
            }
        }
        
        if (number < 100) {
            val tens = number / 10
            val ones = number % 10
            val tensWords = when (tens) {
                2 -> "Veinte"
                3 -> "Treinta"
                4 -> "Cuarenta"
                5 -> "Cincuenta"
                6 -> "Sesenta"
                7 -> "Setenta"
                8 -> "Ochenta"
                9 -> "Noventa"
                else -> ""
            }
            return if (ones > 0) {
                "$tensWords y ${convertNumberToWords(ones).lowercase()}"
            } else {
                tensWords
            }
        }
        
        if (number < 1000) {
            val hundreds = number / 100
            val remainder = number % 100
            val hundredsWords = when (hundreds) {
                1 -> "Cien"
                2 -> "Doscientos"
                3 -> "Trescientos"
                4 -> "Cuatrocientos"
                5 -> "Quinientos"
                6 -> "Seiscientos"
                7 -> "Setecientos"
                8 -> "Ochocientos"
                9 -> "Novecientos"
                else -> ""
            }
            return if (remainder > 0) {
                "$hundredsWords ${convertNumberToWords(remainder).lowercase()}"
            } else {
                hundredsWords
            }
        }
        
        // Para números mayores, simplificar
        return number.toString()
    }

    @Transactional
    fun applyPaymentToBill(bill: com.dreamsbo.posapi.persistence.entity.WaterBillEntity, amount: BigDecimal) {
        bill.paidAmount = bill.paidAmount.add(amount)
        bill.remainingBalance = bill.remainingBalance.subtract(amount)

        // Update bill status
        val newStatus = when {
            bill.remainingBalance <= BigDecimal.ZERO -> {
                bill.paidDate = LocalDate.now()
                billStatusTypeRepository.findByCodeAndActive("PAID", true)
                    .orElseThrow { NotFoundEntityException("Estado PAID no encontrado") }
            }
            bill.paidAmount > BigDecimal.ZERO && bill.remainingBalance > BigDecimal.ZERO -> {
                billStatusTypeRepository.findByCodeAndActive("PARTIAL_PAID", true)
                    .orElseThrow { NotFoundEntityException("Estado PARTIAL_PAID no encontrado") }
            }
            else -> bill.status
        }

        bill.status = newStatus
        bill.updatedAt = OffsetDateTime.now()
        waterBillRepository.save(bill)
    }

    private fun generateReceiptNumber(partnerId: UUID): String {
        val timestamp = System.currentTimeMillis()
        val partnerIdShort = partnerId.toString().substring(0, 8)
        return "RC-$partnerIdShort-$timestamp"
    }

    /**
     * Crea un CashFlow automáticamente cuando se registra un pago de agua.
     * El CashFlow se crea como INGRESO ya que es dinero que entra a la caja.
     */
    private fun createCashFlowFromPayment(
        payment: WaterPaymentEntity,
        cashBalance: com.dreamsbo.posapi.persistence.entity.CashBalanceEntity
    ) {
        // Buscar el tipo de flujo "INGRESO"
        val ingresoTipo = cashFlowTypeRepository.findByNameAndActive("INGRESO", true)
            .orElseGet {
                // Si no se encuentra por nombre, buscar por código "IN"
                cashFlowTypeRepository.findByCodeAndActive("IN", true)
                    .orElseThrow {
                        NotFoundEntityException("No se ha encontrado el tipo de flujo INGRESO. Asegúrese de que exista en la base de datos.")
                    }
            }

        // Crear descripción del cash flow
        val description = "Pago de factura de agua - ${payment.waterBill.billNumber} - Socio: ${payment.partner.fullName}"

        // Crear y guardar el CashFlow
        val cashFlow = CashFlowEntity(
            amount = payment.amount,
            description = description,
            cashBalance = cashBalance,
            cashFlowType = ingresoTipo,
            paymentType = payment.paymentType
        )

        cashFlowRepository.save(cashFlow)
        println("✅ CashFlow creado automáticamente para el pago ${payment.id} - Monto: ${payment.amount}")
    }

    private fun toWaterPaymentOutputDto(entity: WaterPaymentEntity): WaterPaymentOutputDto {
        return WaterPaymentOutputDto(
            id = entity.id,
            waterBillId = entity.waterBill.id,
            billNumber = entity.waterBill.billNumber,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            paymentDate = entity.paymentDate,
            amount = entity.amount,
            paymentTypeName = entity.paymentType.name,
            receiptNumber = entity.receiptNumber,
            cashierName = "${entity.user.profile.name} ${entity.user.profile.lastname}",
            observation = entity.observation,
            createdAt = entity.createdAt
        )
    }
}
