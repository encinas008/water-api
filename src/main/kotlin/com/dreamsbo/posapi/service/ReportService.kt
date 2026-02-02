package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import com.dreamsbo.posapi.persistence.repository.WaterBillRepository
import com.dreamsbo.posapi.persistence.repository.WaterMeterReadingRepository
import com.dreamsbo.posapi.persistence.repository.WaterPaymentRepository
import com.dreamsbo.posapi.persistence.repository.BillConceptItemRepository
import com.dreamsbo.posapi.util.DateUtil
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import com.dreamsbo.posapi.dto.DailyMovementReportDto
import com.dreamsbo.posapi.dto.MovementConceptDto
import com.dreamsbo.posapi.dto.MonthlyReadingsReportDto
import com.dreamsbo.posapi.dto.MonthlyReadingItemDto
import com.dreamsbo.posapi.dto.PartnerStatusReportDto
import com.dreamsbo.posapi.dto.PartnerStatusItemDto

@Service
class ReportService(
    val saleService: SaleService,
    val debtManagementService: DebtManagementService,
    val partnerRepository: PartnerRepository,
    val waterBillRepository: WaterBillRepository,
    val waterPaymentRepository: WaterPaymentRepository,
    val waterMeterReadingRepository: WaterMeterReadingRepository,
    val billConceptItemRepository: BillConceptItemRepository,
    val waterPaymentDetailRepository: com.dreamsbo.posapi.persistence.repository.WaterPaymentDetailRepository,
    val cashFlowRepository: com.dreamsbo.posapi.persistence.repository.CashFlowRepository,
) {

    fun generateTicketKitchen(ticketKitchenInputDto: TicketKitchenInputDto): ByteArray? {

        val saleDetails = saleService.getSaleById(ticketKitchenInputDto.saleId)

        val params: MutableMap<String, Any> = HashMap()
        params["productsDetail"] = JRBeanCollectionDataSource(saleDetails.products)
        params["orderNumber"] = saleDetails.orderNumber?.minus(BigDecimal.ONE).toString()
        params["createdAt"] = DateUtil.simpleFormat(saleDetails.createdAt).uppercase()

        val ticketKitchenSourceFileName = "reports/ticketKitchen.jrxml"

        val jasperPrint = JasperFillManager.fillReport(
            JasperCompileManager.compileReport(ticketKitchenSourceFileName),
            params,
            JREmptyDataSource()
        )

//        return JasperPrintManager.printReport(jasperPrint, true)
        return JasperExportManager.exportReportToPdf(jasperPrint)
    }

    fun generateTicketClient(ticketKitchenInputDto: TicketKitchenInputDto): ByteArray? {

        val saleDetails = saleService.getSaleById(ticketKitchenInputDto.saleId)

        val params: MutableMap<String, Any> = HashMap()
        params["productsDetail"] = JRBeanCollectionDataSource(saleDetails.products)
        params["orderNumber"] = saleDetails.orderNumber?.minus(BigDecimal.ONE).toString()
        params["clientName"] = saleDetails.clientName
        params["total"] = saleDetails.total
        params["paymentType"] = saleDetails.paymentTypeName
        params["createdAt"] = DateUtil.simpleFormat(saleDetails.createdAt).uppercase()

        val ticket = "reports/ticketClient.jrxml"

        val jasperPrint = JasperFillManager.fillReport(
            JasperCompileManager.compileReport(ticket),
            params,
            JREmptyDataSource()
        )

        return JasperExportManager.exportReportToPdf(jasperPrint)
    }

    // Water System Reports

    fun getActivePartnersReport(): List<Map<String, Any>> {
        val partners = partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "fullName"))
        
        return partners.map { partner ->
            mapOf(
                "partnerId" to partner.id,
                "name" to partner.fullName,
                "documentId" to (partner.partnerIdentificationNumber ?: "N/A"),
                "waterMeterNumber" to (partner.waterMeterNumber ?: "N/A"),
                "connectionStatus" to (partner.connectionStatus?.name ?: "Sin estado"),
                "connectionDate" to (partner.connectionDate?.toString() ?: "N/A"),
                "currentDebt" to partner.currentDebt,
                "phone" to partner.cellphone
            )
        }
    }

    fun getConsumptionReport(startDate: LocalDate, endDate: LocalDate): List<ConsumptionReportDto> {
        val bills = waterBillRepository.findByBillingPeriodStartBetweenAndActive(startDate, endDate, true)
        
        return bills.map { bill ->
            ConsumptionReportDto(
                partnerId = bill.partner.id,
                partnerName = bill.partner.fullName,
                month = bill.billingPeriodStart.month.toString(),
                year = bill.billingPeriodStart.year,
                consumption = bill.consumptionM3,
                billedAmount = bill.totalAmount,
                paidAmount = bill.paidAmount,
                status = bill.status.name
            )
        }
    }

    fun getCollectionReport(startDate: LocalDate, endDate: LocalDate): CollectionReportDto {
        val bills = waterBillRepository.findByBillingPeriodStartBetweenAndActive(startDate, endDate, true)
        
        val totalBilled = bills.sumOf { it.totalAmount }
        val totalCollected = bills.sumOf { it.paidAmount }
        val totalPending = bills.sumOf { it.remainingBalance }
        
        val billsGenerated = bills.size
        val billsPaid = bills.count { it.status.code == "PAID" }
        val billsPending = bills.count { it.status.code in listOf("PENDING", "PARTIAL_PAID") }
        val billsOverdue = bills.count { 
            it.dueDate.isBefore(LocalDate.now()) && it.status.code != "PAID" 
        }
        
        val collectionRate = if (totalBilled > BigDecimal.ZERO) {
            (totalCollected.divide(totalBilled, 4, RoundingMode.HALF_UP) * BigDecimal(100)).toDouble()
        } else {
            0.0
        }

        return CollectionReportDto(
            period = "$startDate a $endDate",
            totalBilled = totalBilled,
            totalCollected = totalCollected,
            totalPending = totalPending,
            billsGenerated = billsGenerated,
            billsPaid = billsPaid,
            billsPending = billsPending,
            billsOverdue = billsOverdue,
            collectionRate = collectionRate
        )
    }

    fun getDebtorsReport(): List<DebtReportDto> {
        return debtManagementService.getDebtorsList()
    }

    fun getOverdueDebtorsReport(): List<DebtReportDto> {
        return debtManagementService.getDebtorsWithOverdueBills()
    }

    fun getCutoffCandidatesReport(): List<DebtReportDto> {
        return debtManagementService.getCutoffCandidatesList()
    }

    fun getPendingReadingsReport(): List<PendingReadingsReportDto> {
        val partners = partnerRepository.findAllByActive(true, Sort.unsorted())
        val currentDate = LocalDate.now()
        
        return partners
            .map { partner ->
                val latestReading = waterMeterReadingRepository.findLatestByPartnerId(partner.id, true)
                val lastReadingDate = latestReading.map { it.readingDate }.orElse(null)
                val daysSinceLastReading = lastReadingDate?.let { 
                    ChronoUnit.DAYS.between(it, currentDate).toInt() 
                }

                PendingReadingsReportDto(
                    partnerId = partner.id,
                    partnerName = partner.fullName,
                    waterMeterNumber = partner.waterMeterNumber,
                    lastReadingDate = lastReadingDate,
                    daysSinceLastReading = daysSinceLastReading,
                    address = partner.waterConnectionAddress ?: partner.address,
                    contactPhone = partner.cellphone
                )
            }
            .sortedByDescending { it.daysSinceLastReading ?: Int.MAX_VALUE }
    }

    fun getComparativeConsumptionReport(partnerId: java.util.UUID, months: Int = 6): List<Map<String, Any>> {
        val bills = waterBillRepository.findByPartnerIdAndActive(
            partnerId,
            true,
            Sort.by(Sort.Direction.DESC, "billingPeriodStart")
        ).take(months)

        return bills.map { bill ->
            mapOf(
                "period" to "${bill.billingPeriodStart} / ${bill.billingPeriodEnd}",
                "consumption" to bill.consumptionM3,
                "amount" to bill.totalAmount,
                "status" to bill.status.name,
                "paidAmount" to bill.paidAmount
            )
        }.reversed()
    }

    fun getMovementReport(startDate: LocalDate, endDate: LocalDate): DailyMovementReportDto {
        val incomeByConcept = mutableMapOf<String, BigDecimal>()
        val expenseByConcept = mutableMapOf<String, BigDecimal>()

        // 1. Water Payments
        val payments = waterPaymentRepository.findByPaymentDateBetweenAndActive(startDate, endDate, true, Sort.by("paymentDate", "createdAt"))

        payments.forEach { payment ->
            val fines = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(payment.id, true)
            var totalFines = BigDecimal.ZERO

            fines.forEach { fine ->
                val name = "MULTA: ${fine.fineName}"
                incomeByConcept[name] = (incomeByConcept[name] ?: BigDecimal.ZERO).add(fine.fineAmount)
                totalFines = totalFines.add(fine.fineAmount)
            }

            val basePaid = payment.amount.subtract(totalFines)
            if (basePaid > BigDecimal.ZERO) {
                val bill = payment.waterBill
                if (bill != null) {
                    // Distribution logic
                    val otherPayments = waterPaymentRepository.findByWaterBillIdAndActive(bill.id, true)
                        .filter {
                            it.id != payment.id && (it.paymentDate.isBefore(payment.paymentDate) ||
                                (it.paymentDate == payment.paymentDate && it.createdAt.isBefore(payment.createdAt)))
                        }

                    var offset = otherPayments.sumOf { p ->
                        val pFines = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(p.id, true)
                        val fSum = pFines.sumOf { it.fineAmount }
                        p.amount.subtract(fSum)
                    }

                    val concepts = billConceptItemRepository.findByWaterBillIdAndActive(bill.id, true)
                        .sortedWith { o1, o2 -> getConceptPriority(o1.conceptName).compareTo(getConceptPriority(o2.conceptName)) }

                    var rem = basePaid
                    concepts.forEach { concept ->
                        val conceptAmt = concept.amount
                        val fromOffset = if (offset > BigDecimal.ZERO) {
                            if (offset >= conceptAmt) conceptAmt else offset
                        } else BigDecimal.ZERO

                        offset = offset.subtract(fromOffset)
                        val remainingConcept = conceptAmt.subtract(fromOffset)

                        if (remainingConcept > BigDecimal.ZERO && rem > BigDecimal.ZERO) {
                            val toPay = if (rem >= remainingConcept) remainingConcept else rem
                            incomeByConcept[concept.conceptName] = (incomeByConcept[concept.conceptName] ?: BigDecimal.ZERO).add(toPay)
                            rem = rem.subtract(toPay)
                        }
                    }

                    if (rem > BigDecimal.ZERO) {
                        incomeByConcept["OTROS INGRESOS AGUA"] = (incomeByConcept["OTROS INGRESOS AGUA"] ?: BigDecimal.ZERO).add(rem)
                    }
                } else {
                    incomeByConcept["INSTALACIÓN / OTROS"] = (incomeByConcept["INSTALACIÓN / OTROS"] ?: BigDecimal.ZERO).add(basePaid)
                }
            }
        }

        // 2. Cash Flows (Manual)
        val startDT = startDate.atStartOfDay().atOffset(ZoneOffset.ofHours(-4))
        val endDT = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.ofHours(-4))
        val cashFlows = cashFlowRepository.findByCreatedAtBetweenAndActive(startDT, endDT, true, Sort.by("createdAt"))

        cashFlows.forEach { cf ->
            val category = cf.cashFlowType.name // "INGRESO" or "EGRESO"
            val name = "MANUAL: ${cf.description}"
            if (category == "INGRESO") {
                incomeByConcept[name] = (incomeByConcept[name] ?: BigDecimal.ZERO).add(cf.amount)
            } else {
                expenseByConcept[name] = (expenseByConcept[name] ?: BigDecimal.ZERO).add(cf.amount)
            }
        }

        val conceptsList = mutableListOf<MovementConceptDto>()

        incomeByConcept.forEach { (name, amount) ->
            conceptsList.add(MovementConceptDto(name, "INGRESO", amount, name.startsWith("MANUAL:")))
        }

        expenseByConcept.forEach { (name, amount) ->
            conceptsList.add(MovementConceptDto(name, "EGRESO", amount, name.startsWith("MANUAL:")))
        }

        val totalIncome = incomeByConcept.values.fold(BigDecimal.ZERO, BigDecimal::add)
        val totalExpense = expenseByConcept.values.fold(BigDecimal.ZERO, BigDecimal::add)

        return DailyMovementReportDto(
            concepts = conceptsList.sortedBy { it.type },
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            grandTotal = totalIncome.subtract(totalExpense)
        )
    }

    fun getMonthlyReadingsReport(year: Int, month: Int): MonthlyReadingsReportDto {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)

        val readings = waterMeterReadingRepository.findByReadingDateBetweenAndActive(
            startDate, endDate, true
        ).sortedBy { it.partner.partnerNumber }

        val items = readings.map { reading ->
            MonthlyReadingItemDto(
                partnerNumber = reading.partner.partnerNumber,
                partnerName = reading.partner.fullName,
                readingValue = reading.currentReading,
                readingDate = reading.readingDate
            )
        }

        return MonthlyReadingsReportDto(
            year = year,
            month = month,
            monthName = getMonthName(month).uppercase(),
            readings = items
        )
    }

    fun getMissingReadingsReport(year: Int, month: Int): List<MissingReadingItemDto> {
        // 1. All active partners
        val partners = partnerRepository.findAllByActive(true, Sort.by("partnerNumber"))

        // 2. Readings for the target month
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        val readingsInMonth = waterMeterReadingRepository.findByReadingDateBetweenAndActive(startDate, endDate, true)
        val partnersWithReading = readingsInMonth.map { it.partner.id }.toSet()

        // 3. Filter missing
        val missingPartners = partners.filter { !partnersWithReading.contains(it.id) }

        // 4. Map to DTO
        return missingPartners.map { partner ->
            val latestReading = waterMeterReadingRepository.findLatestByPartnerId(partner.id, true)
            
            MissingReadingItemDto(
                partnerId = partner.id,
                partnerNumber = partner.partnerNumber,
                partnerName = partner.fullName,
                waterMeterNumber = partner.waterMeterNumber,
                previousReading = latestReading.map { it.currentReading }.orElse(null),
                previousReadingDate = latestReading.map { it.readingDate }.orElse(null)
            )
        }
    }

    fun getPartnerStatusReport(): PartnerStatusReportDto {
        val partners = partnerRepository.findAllByActive(true, Sort.by("partnerNumber"))

        val statusSummary = partners.groupBy { it.connectionStatus?.name ?: "SIN ESTADO" }
            .mapValues { it.value.size }

        val results = partners.map { partner ->
            PartnerStatusItemDto(
                partnerNumber = partner.partnerNumber,
                fullName = partner.fullName,
                identificationNumber = partner.partnerIdentificationNumber,
                address = partner.waterConnectionAddress ?: partner.address,
                currentDebt = partner.currentDebt,
                statusName = partner.connectionStatus?.name ?: "SIN ESTADO",
                statusCode = partner.connectionStatus?.code ?: "NONE"
            )
        }

        return PartnerStatusReportDto(statusSummary, results)
    }

    // Water Payment Receipt PDF Generation
    
    fun generateWaterPaymentReceiptPdf(paymentId: UUID): ByteArray {
        val payment = waterPaymentRepository.findById(paymentId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el pago. PaymentId = $paymentId") }
        
        val bill = payment.waterBill
        val partner = payment.partner
        val reading = bill?.reading
        
        // Obtener conceptos de la factura (si existe)
        val billConcepts = bill?.let { 
            billConceptItemRepository.findByWaterBillIdAndActive(it.id, true)
        } ?: emptyList()
        
        // Obtener multas pagadas en este recibo
        val paymentDetails = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(paymentId, true)
        
        // Convertir conceptos de factura a DTO
        val allConceptDtos = billConcepts.map { concept ->
            ConceptReportDto(
                conceptName = concept.conceptName,
                assignedDate = formatSimpleDate(concept.assignedDate),
                amount = concept.amount
            )
        }.toMutableList()
        
        // Agregar multas como conceptos adicionales
        paymentDetails.forEach { detail ->
            allConceptDtos.add( ConceptReportDto(
                conceptName = "MULTA: ${detail.fineName} (${detail.fineType})",
                assignedDate = formatSimpleDate(detail.fineDate),
                amount = detail.fineAmount
            ))
        }

        // Si no hay factura, agregar concepto de instalación
        if (bill == null) {
            allConceptDtos.add(ConceptReportDto(
                conceptName = "INSTALACIÓN DE AGUA",
                assignedDate = formatSimpleDate(payment.paymentDate),
                amount = payment.amount
            ))
        }

        // Ordenar conceptos según requerimiento del usuario
        // 1) Tarifa Basica
        // 2) Aporte a la OTB
        // 3) Aporte al Deporte
        // 4) Multa por exeso de consumo o consumo
        // 5) Reuniones
        // 6) Trabajos
        // 7) Otros ingresos
        allConceptDtos.sortWith(Comparator { o1, o2 ->
            val p1 = getConceptPriority(o1.conceptName)
            val p2 = getConceptPriority(o2.conceptName)
            p1.compareTo(p2)
        })

        // Preparar parámetros
        val params: MutableMap<String, Any> = HashMap()
        params["receiptNumber"] = payment.receiptNumber
        params["correlativeNumber"] = payment.correlativeNumber ?: 0
        params["receiptType"] = "NOTA DE PAGO"
        params["partnerNumber"] = partner.partnerNumber.toString()
        params["partnerName"] = partner.fullName
        params["partnerIdentificationNumber"] = partner.partnerIdentificationNumber ?: ""
        params["paymentDate"] = formatPaymentDateTime(payment.paymentDate)
        params["currentReading"] = reading?.currentReading ?: BigDecimal.ZERO
        params["previousReading"] = reading?.previousReading ?: BigDecimal.ZERO
        params["consumptionM3"] = reading?.consumption ?: BigDecimal.ZERO
        params["meterNumber"] = partner.waterMeterNumber ?: "0"
        
        if (bill != null) {
            params["paymentMonth"] = getMonthName(bill.billingPeriodStart.monthValue).uppercase()
            params["paymentMonthDate"] = formatMonthDate(bill.billingPeriodEnd)
        } else {
            params["paymentMonth"] = "INSTALACIÓN"
            params["paymentMonthDate"] = formatMonthDate(payment.paymentDate)
        }
        
        params["communityName"] = "COMUNIDAD GUADALUPE"
        params["conceptsList"] = allConceptDtos
        
        // El monto total a mostrar debe ser el monto pagado en este recibo.
        params["totalAmount"] = payment.amount 
        params["totalAmountInWords"] = numberToWords(payment.amount)
        
        val reportPath = if (bill != null) {
            "reports/waterPaymentReceipt.jrxml"
        } else {
            "reports/waterInstallationReceipt.jrxml"
        }
        
        val jasperPrint = JasperFillManager.fillReport(
            JasperCompileManager.compileReport(reportPath),
            params,
            JREmptyDataSource()
        )
        
        return JasperExportManager.exportReportToPdf(jasperPrint)
    }

    fun generateCashFlowReceiptPdf(cashFlowId: UUID): ByteArray {
        val cashFlow = cashFlowRepository.findById(cashFlowId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el movimiento. Id = $cashFlowId") }

        val params: MutableMap<String, Any> = HashMap()
        params["movementType"] = cashFlow.cashFlowType.name
        params["amount"] = cashFlow.amount
        params["amountInWords"] = numberToWords(cashFlow.amount)
        params["description"] = cashFlow.description
        params["date"] = formatPaymentDateTime(cashFlow.createdAt.toLocalDate())
        params["userName"] = "${cashFlow.cashBalance.box.user.profile.name} ${cashFlow.cashBalance.box.user.profile.lastname}"
        params["boxName"] = cashFlow.cashBalance.box.name
        params["communityName"] = "COMUNIDAD GUADALUPE"
        params["correlativeNumber"] = cashFlow.correlativeNumber ?: 0

        val reportPath = "reports/cashFlowReceipt.jrxml"

        val jasperPrint = JasperFillManager.fillReport(
            JasperCompileManager.compileReport(reportPath),
            params,
            JREmptyDataSource()
        )

        return JasperExportManager.exportReportToPdf(jasperPrint)
    }

    private fun getConceptPriority(conceptName: String): Int {
        val name = conceptName.uppercase()
        return when {
            name.contains("TARIFA BÁSICA") || name.contains("TARIFA BASICA") -> 1
            name.contains("APORTE A LA OTB") -> 2
            name.contains("APORTE AL DEPORTE") -> 3
            name.contains("EXCESO") || name.contains("CONSUMO") -> 4
            name.contains("MEETING") || name.contains("REUNIÓN") -> 5
            name.contains("JOB") || name.contains("TRABAJO") -> 6
            name.contains("OTROS INGRESOS") -> 7
            else -> 99
        }
    }
    
    private fun formatPaymentDateTime(date: LocalDate): String {
        val now = OffsetDateTime.now(ZoneOffset.ofHours(-4))
        return String.format(
            "%02d:%02d %02d-%s-%d",
            now.hour,
            now.minute,
            date.dayOfMonth,
            getMonthName(date.monthValue),
            date.year
        )
    }
    
    private fun formatMonthDate(date: LocalDate): String {
        return String.format(
            "%02d-%s-%d",
            date.dayOfMonth,
            getMonthName(date.monthValue),
            date.year
        )
    }
    
    private fun formatSimpleDate(date: LocalDate): String {
        return String.format(
            "%02d-%s-%d",
            date.dayOfMonth,
            getMonthName(date.monthValue),
            date.year
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
        
        val wholeWords = convertNumberToWords(wholePart).uppercase()
        val centsString = String.format("%02d", cents)
        
        return "$wholeWords $centsString/100 BOLIVIANOS"
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

        if (number == 1000) return "Mil"

        if (number < 2000) {
             val remainder = number % 1000
             return if (remainder > 0) "Mil " + convertNumberToWords(remainder).lowercase() else "Mil"
        }

        if (number < 1000000) {
             val thousands = number / 1000
             val remainder = number % 1000
             val thousandsWords = convertNumberToWords(thousands)
             return if (remainder > 0) {
                 "$thousandsWords mil ${convertNumberToWords(remainder).lowercase()}"
             } else {
                 "$thousandsWords mil"
             }
        }
        
        // Para números mayores, simplificar
        return number.toString()
    }
    
    // DTO para conceptos en el reporte
    data class ConceptReportDto(
        val conceptName: String,
        val assignedDate: String,
        val amount: BigDecimal
    )
}
