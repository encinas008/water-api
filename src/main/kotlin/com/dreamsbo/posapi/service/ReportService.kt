package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import com.dreamsbo.posapi.persistence.repository.WaterBillRepository
import com.dreamsbo.posapi.persistence.repository.WaterMeterReadingRepository
import com.dreamsbo.posapi.persistence.repository.WaterPaymentRepository
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
import java.time.temporal.ChronoUnit

@Service
class ReportService(
    val saleService: SaleService,
    val debtManagementService: DebtManagementService,
    val partnerRepository: PartnerRepository,
    val waterBillRepository: WaterBillRepository,
    val waterPaymentRepository: WaterPaymentRepository,
    val waterMeterReadingRepository: WaterMeterReadingRepository,
) {

    fun generateTicketKitchen(ticketKitchenInputDto: TicketKitchenInputDto): ByteArray? {

        val saleDetails = saleService.getSaleById(ticketKitchenInputDto.saleId)

        val params: MutableMap<String, Any> = HashMap()
        params["productsDetail"] = JRBeanCollectionDataSource(saleDetails.products)
        params["orderNumber"] = saleDetails.orderNumber?.minus(BigDecimal.ONE).toString()
        params["orderFor"] = saleDetails.orderFor
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
                "documentId" to partner.partnerIdentificationNumber,
                "waterConnectionNumber" to (partner.waterConnectionNumber ?: "N/A"),
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
                waterConnectionNumber = bill.partner.waterConnectionNumber,
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
            .filter { it.waterConnectionNumber != null }
            .map { partner ->
                val latestReading = waterMeterReadingRepository.findLatestByPartnerId(partner.id, true)
                val lastReadingDate = latestReading.map { it.readingDate }.orElse(null)
                val daysSinceLastReading = lastReadingDate?.let { 
                    ChronoUnit.DAYS.between(it, currentDate).toInt() 
                }

                PendingReadingsReportDto(
                    partnerId = partner.id,
                    partnerName = partner.fullName,
                    waterConnectionNumber = partner.waterConnectionNumber,
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
}
