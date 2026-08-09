package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.DebtReportDto
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import com.dreamsbo.posapi.persistence.repository.WaterBillRepository
import com.dreamsbo.posapi.persistence.repository.WaterPaymentRepository
import com.dreamsbo.posapi.persistence.repository.BillConceptItemRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class DebtManagementService(
    private val partnerRepository: PartnerRepository,
    private val waterBillRepository: WaterBillRepository,
    private val waterPaymentRepository: WaterPaymentRepository,
    private val monthlyPendingFinesService: MonthlyPendingFinesService,
    private val billConceptItemRepository: BillConceptItemRepository,
) {

    fun calculateTotalDebt(partnerId: UUID): BigDecimal {
        // 1. Sum remaining balance of all unpaid bills (PENDING, OVERDUE, PARTIAL_PAID)
        val unpaidBills = waterBillRepository.findByPartnerIdAndStatusCodesInAndActive(
            partnerId, listOf("PENDING"), true
        )
        val billsDebt = unpaidBills.sumOf { it.remainingBalance }

        // 2. Sum fines for months that don't have a PAID bill
        // First, get months that ARE paid
        val paidMonths = waterBillRepository.findByPartnerIdAndActive(partnerId, true, Sort.unsorted())
            .filter { it.status.code == "PAID" }
            .map { "${it.billingPeriodStart.monthValue}-${it.billingPeriodStart.year}" }
            .toSet()

        // Get all unpaid fines recorded in the system
        val allUnpaidFines = monthlyPendingFinesService.getAllUnpaidFines(partnerId)
        
        // Filter out fines where the month is already settled via a PAID bill
        // Also skip fines that are ALREADY included in a PENDING/OVERDUE bill to avoid double counting
        
        // OPTIMIZATION: Fetch ALL concepts for these bills in ONE query instead of N queries
        val billIds = unpaidBills.map { it.id }
        val allConcepts = if (billIds.isNotEmpty()) {
            billConceptItemRepository.findByWaterBillIdInAndActive(billIds, true)
        } else {
            emptyList()
        }
        
        // Create a set of concept names (normalized)
        val billedFineConcepts = allConcepts.map { it.conceptName.lowercase().trim() }.toSet()

        val unbilledFinesDebt = allUnpaidFines.filter { fine ->
            val monthYear = "${fine.date.monthValue}-${fine.date.year}"
            val isNotPaidMonth = !paidMonths.contains(monthYear)
            
            // Optimization: check against the Set of concepts
            val isNotYetBilled = billedFineConcepts.none { it.contains(fine.name.lowercase().trim()) }
            
            isNotPaidMonth && isNotYetBilled
        }.sumOf { it.fine }

        return billsDebt.add(unbilledFinesDebt)
    }

    fun getDebtorsList(): List<DebtReportDto> {
        val allPartners = partnerRepository.findAllByActive(true, Sort.unsorted())
        
        return allPartners
            .map { partner ->
                val currentDebt = calculateTotalDebt(partner.id)
                val bills = waterBillRepository.findByPartnerIdAndActive(partner.id, true, Sort.unsorted())
                val pendingBills = bills.filter { it.status.code == "PENDING" }
                val overdueBills = bills.filter { 
                    it.status.code == "OVERDUE" || (it.dueDate.isBefore(LocalDate.now()) && it.status.code != "PAID") 
                }
                
                val oldestBill = bills
                    .filter { it.status.code != "PAID" }
                    .minByOrNull { it.billingPeriodStart }
                
                val payments = waterPaymentRepository.findByPartnerIdAndActive(
                    partner.id, 
                    true, 
                    Sort.by(Sort.Direction.DESC, "paymentDate")
                )
                val lastPaymentDate = payments.firstOrNull()?.paymentDate

                DebtReportDto(
                    partnerId = partner.id,
                    partnerNumber = partner.partnerNumber,
                    partnerName = partner.fullName,
                    partnerIdentificationNumber = partner.partnerIdentificationNumber,
                    totalDebt = currentDebt,
                    pendingBillsCount = pendingBills.size,
                    overdueBillsCount = overdueBills.size,
                    connectionStatus = partner.connectionStatus?.name ?: "N/A",
                    lastPaymentDate = lastPaymentDate,
                    contactPhone = partner.cellphone
                )
            }
            .filter { it.totalDebt > BigDecimal.ZERO }
            .sortedByDescending { it.totalDebt }
    }

    fun getDebtorsWithOverdueBills(): List<DebtReportDto> {
        return getDebtorsList().filter { it.overdueBillsCount > 0 }
    }

    fun getCutoffCandidatesList(): List<DebtReportDto> {
        // 1. Get only partners with 4+ pending bills directly from DB (FAST)
        val candidateIds = waterBillRepository.findPartnerIdsWithPendingBillsCount(4)
        
        if (candidateIds.isEmpty()) {
            return emptyList()
        }

        // 2. Load full data only for candidates (Efficiency: O(Candidates) instead of O(All Partners))
        val candidates = partnerRepository.findAllById(candidateIds)
        
        return candidates.map { partner ->
            val bills = waterBillRepository.findByPartnerIdAndActive(partner.id, true, Sort.unsorted())
            val pendingBills = bills.filter { it.status.code == "PENDING" }
            val overdueBills = bills.filter { 
                it.status.code == "OVERDUE" || (it.dueDate.isBefore(LocalDate.now()) && it.status.code != "PAID") 
            }
            
            // Re-check count (just in case status code logic differs slightly in Kotlin vs SQL)
            if (pendingBills.size < 4) return@map null

            val currentDebt = calculateTotalDebt(partner.id)
            
            val oldestBill = bills
                .filter { it.status.code != "PAID" }
                .minByOrNull { it.billingPeriodStart }
            
            val payments = waterPaymentRepository.findByPartnerIdAndActive(
                partner.id, 
                true, 
                Sort.by(Sort.Direction.DESC, "paymentDate")
            )
            val lastPaymentDate = payments.firstOrNull()?.paymentDate

            DebtReportDto(
                partnerId = partner.id,
                partnerNumber = partner.partnerNumber,
                partnerName = partner.fullName,
                partnerIdentificationNumber = partner.partnerIdentificationNumber,
                totalDebt = currentDebt,
                pendingBillsCount = pendingBills.size,
                overdueBillsCount = overdueBills.size,
                connectionStatus = partner.connectionStatus?.name ?: "N/A",
                lastPaymentDate = lastPaymentDate,
                contactPhone = partner.cellphone
            )
        }.filterNotNull()
         .sortedByDescending { it.totalDebt }
    }

    fun getMoraCandidatesList(months: Int = 6): List<DebtReportDto> {
        val startDate = LocalDate.now().minusMonths(months.toLong())
        val candidateIds = waterBillRepository.findPartnerIdsWithPendingConceptInPeriod("mora", startDate)
        
        if (candidateIds.isEmpty()) {
            return emptyList()
        }

        val candidates = partnerRepository.findAllById(candidateIds)
        
        return candidates.mapNotNull { partner ->
            val bills = waterBillRepository.findByPartnerIdAndActive(partner.id, true, Sort.unsorted())
            val pendingBills = bills.filter { it.status.code == "PENDING" }
            val overdueBills = bills.filter { 
                it.status.code == "OVERDUE" || (it.dueDate.isBefore(LocalDate.now()) && it.status.code != "PAID") 
            }


            val currentDebt = calculateTotalDebt(partner.id)
            
            val payments = waterPaymentRepository.findByPartnerIdAndActive(
                partner.id, 
                true, 
                Sort.by(Sort.Direction.DESC, "paymentDate")
            )
            val lastPaymentDate = payments.firstOrNull()?.paymentDate

            val pendingMonthsStr = pendingBills
                .sortedBy { it.billingPeriodStart }
                .joinToString(", ") { "${it.billingPeriodStart.monthValue}/${it.billingPeriodStart.year}" }

            DebtReportDto(
                partnerId = partner.id,
                partnerNumber = partner.partnerNumber,
                partnerName = partner.fullName,
                partnerIdentificationNumber = partner.partnerIdentificationNumber,
                totalDebt = currentDebt,
                pendingBillsCount = pendingBills.size,
                overdueBillsCount = overdueBills.size,
                connectionStatus = partner.connectionStatus?.name ?: "N/A",
                lastPaymentDate = lastPaymentDate,
                contactPhone = partner.cellphone,
                pendingMonths = pendingMonthsStr
            )
        }.sortedByDescending { it.totalDebt }
    }

    @Transactional
    fun updatePartnerDebt(partnerId: UUID) {
        val partner = partnerRepository.findById(partnerId).orElse(null) ?: return
        
        val totalDebt = calculateTotalDebt(partnerId)
        partner.currentDebt = totalDebt
        
        partnerRepository.save(partner)
    }

    @Transactional
    fun updateAllPartnersDebt() {
        val allPartners = partnerRepository.findAllByActive(true, Sort.unsorted())
        
        allPartners.forEach { partner ->
            val totalDebt = calculateTotalDebt(partner.id)
            partner.currentDebt = totalDebt
        }
        
        partnerRepository.saveAll(allPartners)
    }

    fun getPartnerDebtSummary(partnerId: UUID): Map<String, Any> {
        val partner = partnerRepository.findById(partnerId).orElse(null) 
            ?: return emptyMap()

        val currentRealDebt = calculateTotalDebt(partnerId)
        val bills = waterBillRepository.findByPartnerIdAndActive(partnerId, true, Sort.unsorted())
        val pendingBills = bills.filter { it.status.code == "PENDING" }
        val paidBills = bills.filter { it.status.code == "PAID" }

        val totalBilled = bills.sumOf { it.totalAmount }
        val totalPaid = bills.sumOf { it.paidAmount }
        val totalPending = pendingBills.sumOf { it.remainingBalance }

        return mapOf(
            "partnerId" to partner.id,
            "partnerName" to partner.fullName,
            "currentDebt" to currentRealDebt,
            "totalBillsCount" to bills.size,
            "pendingBillsCount" to pendingBills.size,
            "paidBillsCount" to paidBills.size,
            "totalBilled" to totalBilled,
            "totalPaid" to totalPaid,
            "totalPending" to totalPending
        )
    }
}
