package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.DetailedDebtItemDto
import com.dreamsbo.posapi.dto.DetailedDebtorsReportDto
import com.dreamsbo.posapi.persistence.repository.BillConceptItemRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import com.dreamsbo.posapi.persistence.repository.WaterBillRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Service
class WaterReportService(
    private val partnerRepository: PartnerRepository,
    private val waterBillRepository: WaterBillRepository,
    private val billConceptItemRepository: BillConceptItemRepository,
    private val monthlyPendingFinesService: MonthlyPendingFinesService,
    private val debtManagementService: DebtManagementService
) {

    fun getDetailedDebtorsReport(search: String? = null): DetailedDebtorsReportDto {
        // 1. Fetch EVERYTHING in bulk for performance
        var allActivePartners = partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.ASC, "partnerNumber"))
        
        if (!search.isNullOrBlank()) {
            val s = search.lowercase()
            allActivePartners = allActivePartners.filter { 
                it.fullName.lowercase().contains(s) || it.partnerNumber?.toString() == s 
            }.toMutableList()
        }

        if (allActivePartners.isEmpty()) {
            return DetailedDebtorsReportDto(
                items = emptyList(),
                totalDebt = BigDecimal.ZERO,
                totalElements = 0,
                totalPages = 1,
                currentPage = 0,
                generatedAt = OffsetDateTime.now()
            )
        }

        val activePartnerIds = allActivePartners.map { it.id }.toSet()

        // Fetch all unpaid bills for all active partners in one shot
        val allUnpaidBills = waterBillRepository.findByStatusCodesAndActive(
            listOf("PENDING"), true
        ).filter { it.partner.id in activePartnerIds }
        val billsByPartner = allUnpaidBills.groupBy { it.partner.id }

        // Fetch all concepts for these bills in one shot
        val billIds = allUnpaidBills.map { it.id }.toSet()
        val allConcepts = billConceptItemRepository.findAll().filter { it.waterBill.id in billIds && it.active }
        val conceptsByBill = allConcepts.groupBy { it.waterBill.id }

        // Fetch all paid bills to check for settled months (to filter fines)
        val allPaidBills = waterBillRepository.findByStatusCodeAndActive("PAID", true)
            .filter { it.partner.id in activePartnerIds }
        val paidMonthsByPartner = allPaidBills.groupBy { it.partner.id }.mapValues { entry ->
            entry.value.map { "${it.billingPeriodStart.monthValue}-${it.billingPeriodStart.year}" }.toSet()
        }

        val reportItems = mutableListOf<DetailedDebtItemDto>()
        var reportTotalDebt = BigDecimal.ZERO

        allActivePartners.forEach { partner ->
            val partnerItems = mutableListOf<DetailedDebtItemDto>()
            val partnerId = partner.id
            
            // A. Add items from bills
            billsByPartner[partnerId]?.forEach { bill ->
                val concepts = conceptsByBill[bill.id] ?: emptyList()
                var conceptsSum = BigDecimal.ZERO
                
                concepts.forEach { concept ->
                    conceptsSum = conceptsSum.add(concept.amount)
                    partnerItems.add(
                        DetailedDebtItemDto(
                            partnerId = partner.id,
                            partnerNumber = partner.partnerNumber?.toString() ?: "N/A",
                            partnerName = partner.fullName,
                            date = bill.billingPeriodStart,
                            concept = concept.conceptName,
                            amount = concept.amount,
                            type = "BILL_CONCEPT"
                        )
                    )
                }
                
                val baseAmount = bill.totalAmount.subtract(conceptsSum)
                if (baseAmount > BigDecimal.ZERO) {
                    partnerItems.add(
                        DetailedDebtItemDto(
                            partnerId = partner.id,
                            partnerNumber = partner.partnerNumber?.toString() ?: "N/A",
                            partnerName = partner.fullName,
                            date = bill.billingPeriodStart,
                            concept = "CONSUMO DE AGUA / CARGO FIJO",
                            amount = baseAmount,
                            type = "BILL_BASE"
                        )
                    )
                }

                if (bill.paidAmount > BigDecimal.ZERO) {
                    partnerItems.add(
                        DetailedDebtItemDto(
                            partnerId = partner.id,
                            partnerNumber = partner.partnerNumber?.toString() ?: "N/A",
                            partnerName = partner.fullName,
                            date = bill.billingPeriodStart,
                            concept = "A CUENTA: Pago parcial de factura ${bill.billNumber}",
                            amount = bill.paidAmount.negate(),
                            type = "PARTIAL_PAYMENT"
                        )
                    )
                }
            }

            // B. Add items from unbilled fines
            val unpaidFines = monthlyPendingFinesService.getAllUnpaidFines(partnerId)
            val billedFineNames = partnerItems.map { it.concept.lowercase().trim() }
            val paidMonths = paidMonthsByPartner[partnerId] ?: emptySet()

            unpaidFines.forEach { fine ->
                val monthYear = "${fine.date.monthValue}-${fine.date.year}"
                val isNotPaidMonth = !paidMonths.contains(monthYear)
                val isNotYetBilled = billedFineNames.none { it.contains(fine.name.lowercase().trim()) }

                if (isNotPaidMonth && isNotYetBilled) {
                    partnerItems.add(
                        DetailedDebtItemDto(
                            partnerId = partner.id,
                            partnerNumber = partner.partnerNumber?.toString() ?: "N/A",
                            partnerName = partner.fullName,
                            date = fine.date,
                            concept = "MULTA: ${fine.name} (${fine.type})",
                            amount = fine.fine,
                            type = "ABSENCE_FINE"
                        )
                    )
                }
            }
            
            // C. Compare reconstructed debt with REAL dynamic debt and add difference as "Migration/Balance"
            val totalRealDebt = debtManagementService.calculateTotalDebt(partnerId)
            val reconstructedDebt = partnerItems.sumOf { it.amount }
            val balanceDifference = totalRealDebt.subtract(reconstructedDebt)
            
            if (balanceDifference > BigDecimal("0.01")) {
                partnerItems.add(
                    DetailedDebtItemDto(
                        partnerId = partner.id,
                        partnerNumber = partner.partnerNumber?.toString() ?: "N/A",
                        partnerName = partner.fullName,
                        date = partner.createdAt.toLocalDate(),
                        concept = "SALDO PENDIENTE (ANTERIOR / MIGRACIÓN)",
                        amount = balanceDifference,
                        type = "MIGRATION_DEBT"
                    )
                )
            }
            
            if (totalRealDebt > BigDecimal.ZERO || partnerItems.isNotEmpty()) {
                reportItems.addAll(partnerItems)
                // Re-calculate sum for safety
                reportTotalDebt = reportTotalDebt.add(partnerItems.sumOf { it.amount })
            }
        }

        return DetailedDebtorsReportDto(
            items = reportItems,
            totalDebt = reportTotalDebt,
            totalElements = reportItems.distinctBy { it.partnerId }.size.toLong(),
            totalPages = 1,
            currentPage = 0,
            generatedAt = OffsetDateTime.now()
        )
    }
}
