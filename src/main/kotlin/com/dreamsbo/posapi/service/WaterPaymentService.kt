package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.BillConceptItemDto
import com.dreamsbo.posapi.dto.PaymentReceiptDto
import com.dreamsbo.posapi.dto.PaymentReceiptFullDto
import com.dreamsbo.posapi.dto.WaterPaymentInputDto
import com.dreamsbo.posapi.dto.WaterPaymentOutputDto
import com.dreamsbo.posapi.dto.PaymentDetailDto
import com.dreamsbo.posapi.dto.PaymentFineDetailDto
import com.dreamsbo.posapi.dto.MonthlyPendingFinesDto
import com.dreamsbo.posapi.persistence.entity.WaterPaymentEntity
import com.dreamsbo.posapi.persistence.entity.WaterPaymentDetailEntity
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
    private val monthlyPendingFinesService: MonthlyPendingFinesService,
    private val waterPaymentDetailRepository: WaterPaymentDetailRepository,
) {

    @Transactional
    fun recordPayment(input: WaterPaymentInputDto): WaterPaymentOutputDto {
        val bill = input.waterBillId?.let {
            waterBillRepository.findById(it)
                .orElseThrow { NotFoundEntityException("No se ha encontrado la factura. BillId = $it") }
        }

        val partner = partnerRepository.findById(input.partnerId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el socio. PartnerId = ${input.partnerId}") }

        val paymentType = paymentTypeRepository.findById(input.paymentTypeId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el tipo de pago. PaymentTypeId = ${input.paymentTypeId}") }

        val user = userRepository.findById(input.userId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el usuario. UserId = $input.userId") }

        val cashBalanceOptional = input.cashBalanceId?.let {
            cashBalanceRepository.findById(it)
        }

        // Calcular monto total y multas pendientes si se solicita
        var totalPaymentAmount = input.amount
        var pendingFinesAmount = BigDecimal.ZERO
        var pendingFines: MonthlyPendingFinesDto? = null
        var billAmount = input.amount // Monto de la factura (sin multas)
        
        if (input.includePendingFines && bill != null) {
            pendingFines = monthlyPendingFinesService.getMonthlyPendingFines(
                input.partnerId, 
                bill.billingPeriodStart.monthValue, 
                bill.billingPeriodStart.year
            )
            pendingFinesAmount = pendingFines.totalFines
            
            // El amount que viene del frontend ya incluye las multas cuando includePendingFines es true
            // Por lo tanto, el monto de la factura es el total menos las multas
            billAmount = input.amount.subtract(pendingFinesAmount)
            totalPaymentAmount = input.amount // El total ya viene en input.amount
        }

        // Validate payment amount
        if (input.amount <= BigDecimal.ZERO) {
            throw BadRequestException("El monto del pago debe ser mayor a cero")
        }

        // Validar que el monto de factura no exceda el saldo pendiente (si hay factura)
        if (bill != null && billAmount > bill.remainingBalance) {
            throw BadRequestException("El monto de la factura (${billAmount}) no puede ser mayor al saldo pendiente (${bill.remainingBalance})")
        }

        // Validar que el monto total no exceda el máximo permitido (si hay factura)
        if (bill != null) {
            val maxAllowedAmount = bill.remainingBalance.add(pendingFinesAmount)
            if (totalPaymentAmount > maxAllowedAmount) {
                throw BadRequestException("El monto total del pago (${totalPaymentAmount}) no puede ser mayor al saldo pendiente de la factura (${bill.remainingBalance}) más las multas pendientes (${pendingFinesAmount})")
            }
        }

        val receiptNumber = generateReceiptNumber(partner.id)

        // Construir observación incluyendo información de multas si aplica
        val observationText = if (input.includePendingFines && pendingFinesAmount > BigDecimal.ZERO) {
            val finesInfo = "Incluye multas del mes: ${pendingFinesAmount}. "
            if (input.observation.isNotEmpty()) {
                finesInfo + input.observation
            } else {
                finesInfo
            }
        } else {
            input.observation
        }

        // Obtener y actualizar el correlativo de la sesión si existe (con bloqueo para concurrencia)
        var correlativeNumber: Int? = null
        input.cashBalanceId?.let { cashBalanceId ->
            val cashBalance = cashBalanceRepository.findByIdLocked(cashBalanceId)
                .orElseThrow { NotFoundEntityException("Balance de caja no encontrada!") }

            cashBalance.lastCorrelative += 1
            correlativeNumber = cashBalance.lastCorrelative
            cashBalanceRepository.save(cashBalance)
        }

        val payment = WaterPaymentEntity(
            waterBill = bill,
            partner = partner,
            paymentDate = input.paymentDate,
            amount = totalPaymentAmount, // Usar el monto total que incluye multas
            paymentType = paymentType,
            cashBalance = cashBalanceOptional?.orElse(null),
            user = user,
            receiptNumber = receiptNumber,
            observation = observationText,
            correlativeNumber = correlativeNumber
        )

        val savedPayment = waterPaymentRepository.save(payment)

        // Update bill amounts and status (si hay factura)
        bill?.let { 
            applyPaymentToBill(it, billAmount)
        }

        // Update partner debt (Total del pago: Factura + Multas)
        partner.currentDebt = partner.currentDebt.subtract(totalPaymentAmount)
        partnerRepository.save(partner)

        // NOTA: No creamos CashFlow automáticamente desde los pagos de agua
        // porque los pagos ya se cuentan en el cálculo del total en caja (cashFromSales.cash)
        // Si se creara un CashFlow aquí, se estaría duplicando el monto en el cálculo

        // Guardar detalle de pago si incluye multas
        if (input.includePendingFines && pendingFines != null && pendingFinesAmount > BigDecimal.ZERO) {
            // Guardar multas de trabajos
            pendingFines.jobAbsences.forEach { absence ->
                val detail = WaterPaymentDetailEntity(
                    waterPayment = savedPayment,
                    fineType = "JOB",
                    fineId = absence.id,
                    fineName = absence.name,
                    fineDate = absence.date,
                    fineAmount = absence.fine
                )
                waterPaymentDetailRepository.save(detail)
            }
            
            // Guardar multas de reuniones
            pendingFines.meetingAbsences.forEach { absence ->
                val detail = WaterPaymentDetailEntity(
                    waterPayment = savedPayment,
                    fineType = "MEETING",
                    fineId = absence.id,
                    fineName = absence.name,
                    fineDate = absence.date,
                    fineAmount = absence.fine
                )
                waterPaymentDetailRepository.save(detail)
            }
        }

        return toWaterPaymentOutputDto(savedPayment)
    }

    fun getAllPayments(): List<WaterPaymentOutputDto> {
        val payments = waterPaymentRepository.findByActive(
            true,
            Sort.by(Sort.Direction.DESC, "paymentDate")
        )
        return payments.map { toWaterPaymentOutputDto(it) }
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

        // Formato: "Enero 2025" (solo mes y año) o "INSTALACIÓN"
        val billingPeriod = if (bill != null) {
            val monthName = getMonthName(bill.billingPeriodStart.monthValue)
            val capitalizedMonth = monthName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            "$capitalizedMonth ${bill.billingPeriodStart.year}"
        } else {
            "INSTALACIÓN DE AGUA"
        }

        val previousBalance = if (bill != null) bill.remainingBalance.add(payment.amount) else BigDecimal.ZERO
        val newBalance = bill?.remainingBalance ?: BigDecimal.ZERO

        return PaymentReceiptDto(
            receiptNumber = payment.receiptNumber,
            paymentDate = payment.paymentDate,
            partnerName = partner.fullName,
            partnerIdentificationNumber = partner.partnerIdentificationNumber,
            billNumber = bill?.billNumber ?: "N/A",
            billingPeriod = billingPeriod,
            amount = payment.amount,
            paymentTypeName = payment.paymentType.name,
            cashierName = "${payment.user.profile.name} ${payment.user.profile.lastname}",
            previousBalance = previousBalance,
            newBalance = newBalance,
            correlativeNumber = payment.correlativeNumber
        )
    }

    fun generateFullReceipt(paymentId: UUID, receiptType: String = "NOTA DE PAGO"): PaymentReceiptFullDto {
        val payment = waterPaymentRepository.findById(paymentId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el pago. PaymentId = $paymentId") }

        val bill = payment.waterBill
        val partner = payment.partner
        val reading = bill?.reading

        // Obtener conceptos de la factura (si hay)
        val concepts = if (bill != null) {
            billConceptItemRepository.findByWaterBillIdAndActive(bill.id, true)
                .map { concept ->
                    BillConceptItemDto(
                        id = concept.id,
                        conceptName = concept.conceptName,
                        assignedDate = concept.assignedDate,
                        amount = concept.amount
                    )
                }.toMutableList()
        } else {
            mutableListOf(BillConceptItemDto(
                id = UUID.randomUUID(),
                conceptName = "INSTALACIÓN DE AGUA",
                assignedDate = payment.paymentDate,
                amount = payment.amount
            ))
        }

        // Obtener multas pagadas en este recibo
        val paymentDetails = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(paymentId, true)
        
        // Agregar multas como conceptos adicionales
        paymentDetails.forEach { detail ->
            concepts.add(BillConceptItemDto(
                id = UUID.randomUUID(), // ID temporal para el DTO
                conceptName = "MULTA: ${detail.fineName} (${detail.fineType})",
                assignedDate = detail.fineDate,
                amount = detail.fineAmount
            ))
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

        // Obtener mes de pago o descripción
        val paymentMonth = if (bill != null) {
            getMonthName(bill.billingPeriodStart.monthValue).uppercase()
        } else {
            "INSTALACIÓN"
        }
        
        val paymentMonthDate = if (bill != null) {
            String.format(
                "%02d-%s-%d",
                bill.billingPeriodEnd.dayOfMonth,
                getMonthName(bill.billingPeriodEnd.monthValue),
                bill.billingPeriodEnd.year
            )
        } else {
            String.format(
                "%02d-%s-%d",
                payment.paymentDate.dayOfMonth,
                getMonthName(payment.paymentDate.monthValue),
                payment.paymentDate.year
            )
        }

        // Lecturas del medidor
        val currentReading = reading?.currentReading ?: BigDecimal.ZERO
        val previousReading = reading?.previousReading ?: BigDecimal.ZERO
        val consumption = reading?.consumption ?: BigDecimal.ZERO

        // El monto total debe ser el del pago (Factura + Multas)
        val totalPaymentAmount = payment.amount
        val totalInWords = numberToWords(totalPaymentAmount)

        return PaymentReceiptFullDto(
            receiptNumber = payment.receiptNumber,
            receiptType = receiptType,
            partnerNumber = partner.partnerNumber.toString(),
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
            totalAmount = totalPaymentAmount,
            totalAmountInWords = totalInWords,
            correlativeNumber = payment.correlativeNumber
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
    fun getPaymentsByBillId(billId: UUID): List<WaterPaymentOutputDto> {
        val payments = waterPaymentRepository.findByWaterBillIdAndActive(billId, true)
            .sortedByDescending { it.paymentDate }
        return payments.map { toWaterPaymentOutputDto(it) }
    }

    private fun toWaterPaymentOutputDto(entity: WaterPaymentEntity): WaterPaymentOutputDto {
        // Obtener detalle de pago desde la base de datos
        val paymentDetails = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(entity.id, true)
        
        val paymentDetail = if (paymentDetails.isNotEmpty()) {
            val jobFines = paymentDetails
                .filter { it.fineType == "JOB" }
                .map {
                    PaymentFineDetailDto(
                        id = it.fineId,
                        type = it.fineType,
                        name = it.fineName,
                        date = it.fineDate,
                        fineAmount = it.fineAmount
                    )
                }
            
            val meetingFines = paymentDetails
                .filter { it.fineType == "MEETING" }
                .map {
                    PaymentFineDetailDto(
                        id = it.fineId,
                        type = it.fineType,
                        name = it.fineName,
                        date = it.fineDate,
                        fineAmount = it.fineAmount
                    )
                }
            
            val finesAmount = paymentDetails.sumOf { it.fineAmount }
            val billAmount = entity.amount.subtract(finesAmount)
            
            PaymentDetailDto(
                billAmount = billAmount,
                finesAmount = finesAmount,
                totalAmount = entity.amount,
                jobFines = jobFines,
                meetingFines = meetingFines
            )
        } else {
            // Si no hay detalles, asumir que todo es monto de factura
            PaymentDetailDto(
                billAmount = entity.amount,
                finesAmount = BigDecimal.ZERO,
                totalAmount = entity.amount,
                jobFines = emptyList(),
                meetingFines = emptyList()
            )
        }
        
        return WaterPaymentOutputDto(
            id = entity.id,
            waterBillId = entity.waterBill?.id,
            billNumber = entity.waterBill?.billNumber,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            paymentDate = entity.paymentDate,
            amount = entity.amount,
            paymentTypeName = entity.paymentType.name,
            receiptNumber = entity.receiptNumber,
            cashierName = "${entity.user.profile.name} ${entity.user.profile.lastname}",
            observation = entity.observation,
            correlativeNumber = entity.correlativeNumber,
            paymentDetail = paymentDetail,
            createdAt = entity.createdAt
        )
    }
}
