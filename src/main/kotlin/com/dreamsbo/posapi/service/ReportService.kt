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

    // Water Payment Receipt PDF Generation
    
    fun generateWaterPaymentReceiptPdf(paymentId: UUID): ByteArray {
        val payment = waterPaymentRepository.findById(paymentId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el pago. PaymentId = $paymentId") }
        
        val bill = payment.waterBill
        val partner = payment.partner
        val reading = bill.reading
        
        // Obtener conceptos de la factura
        val billConcepts = billConceptItemRepository.findByWaterBillIdAndActive(bill.id, true)
        
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

        // Preparar parámetros
        val params: MutableMap<String, Any> = HashMap()
        params["receiptNumber"] = payment.receiptNumber
        params["receiptType"] = "NOTA DE PAGO"
        params["partnerNumber"] = partner.partnerNumber.toString()
        params["partnerName"] = partner.fullName
        params["partnerIdentificationNumber"] = partner.partnerIdentificationNumber ?: ""
        params["paymentDate"] = formatPaymentDateTime(payment.paymentDate)
        params["currentReading"] = reading?.currentReading ?: BigDecimal.ZERO
        params["previousReading"] = reading?.previousReading ?: BigDecimal.ZERO
        params["consumptionM3"] = reading?.consumption ?: BigDecimal.ZERO
        params["meterNumber"] = partner.waterMeterNumber ?: "0"
        params["paymentMonth"] = getMonthName(bill.billingPeriodStart.monthValue).uppercase()
        params["paymentMonthDate"] = formatMonthDate(bill.billingPeriodEnd)
        params["communityName"] = "COMUNIDAD GUADALUPE"
        params["conceptsList"] = allConceptDtos
        
        // El monto total a mostrar debe ser el monto pagado en este recibo.
        params["totalAmount"] = payment.amount 
        params["totalAmountInWords"] = numberToWords(payment.amount)
        
        val reportPath = "reports/waterPaymentReceipt.jrxml"
        
        val jasperPrint = JasperFillManager.fillReport(
            JasperCompileManager.compileReport(reportPath),
            params,
            JREmptyDataSource()
        )
        
        return JasperExportManager.exportReportToPdf(jasperPrint)
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
    
    // DTO para conceptos en el reporte
    data class ConceptReportDto(
        val conceptName: String,
        val assignedDate: String,
        val amount: BigDecimal
    )
}
