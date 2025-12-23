package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.DebtReportDto
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import com.dreamsbo.posapi.persistence.repository.WaterBillRepository
import com.dreamsbo.posapi.persistence.repository.WaterPaymentRepository
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
) {

    fun calculateTotalDebt(partnerId: UUID): BigDecimal {
        val bills = waterBillRepository.findByPartnerIdAndActive(partnerId, true, Sort.unsorted())
        return bills
            .filter { it.status.code in listOf("PENDING", "PARTIAL_PAID", "OVERDUE") }
            .sumOf { it.remainingBalance }
    }

    fun getDebtorsList(): List<DebtReportDto> {
        val allPartners = partnerRepository.findAllByActive(true, Sort.unsorted())
        
        return allPartners
            .filter { it.currentDebt > BigDecimal.ZERO }
            .map { partner ->
                val bills = waterBillRepository.findByPartnerIdAndActive(partner.id, true, Sort.unsorted())
                val pendingBills = bills.filter { it.status.code in listOf("PENDING", "PARTIAL_PAID") }
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
                    partnerName = partner.fullName,
                    partnerIdentificationNumber = partner.partnerIdentificationNumber,
                    waterConnectionNumber = partner.waterConnectionNumber,
                    totalDebt = partner.currentDebt,
                    pendingBillsCount = pendingBills.size,
                    overdueBillsCount = overdueBills.size,
                    oldestDebtDate = oldestBill?.billingPeriodStart,
                    connectionStatus = partner.connectionStatus?.name ?: "N/A",
                    lastPaymentDate = lastPaymentDate,
                    contactPhone = partner.cellphone
                )
            }
            .sortedByDescending { it.totalDebt }
    }

    fun getDebtorsWithOverdueBills(): List<DebtReportDto> {
        return getDebtorsList().filter { it.overdueBillsCount > 0 }
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

        val bills = waterBillRepository.findByPartnerIdAndActive(partnerId, true, Sort.unsorted())
        val pendingBills = bills.filter { it.status.code in listOf("PENDING", "PARTIAL_PAID", "OVERDUE") }
        val paidBills = bills.filter { it.status.code == "PAID" }

        val totalBilled = bills.sumOf { it.totalAmount }
        val totalPaid = bills.sumOf { it.paidAmount }
        val totalPending = pendingBills.sumOf { it.remainingBalance }

        return mapOf(
            "partnerId" to partner.id,
            "partnerName" to partner.fullName,
            "currentDebt" to partner.currentDebt,
            "totalBillsCount" to bills.size,
            "pendingBillsCount" to pendingBills.size,
            "paidBillsCount" to paidBills.size,
            "totalBilled" to totalBilled,
            "totalPaid" to totalPaid,
            "totalPending" to totalPending
        )
    }
}
