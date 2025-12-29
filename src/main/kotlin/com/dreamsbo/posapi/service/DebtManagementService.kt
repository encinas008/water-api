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
    private val monthlyPendingFinesService: MonthlyPendingFinesService,
) {

    fun calculateTotalDebt(partnerId: UUID): BigDecimal {
        val bills = waterBillRepository.findByPartnerIdAndActive(partnerId, true, Sort.unsorted())
        val pendingBills = bills.filter { it.status.code == "PENDING" }
        
        val billsDebt = pendingBills.sumOf { it.remainingBalance }
            
        // Sumar las multas de cada mes que tiene una factura pendiente
        var totalFinesDebt = BigDecimal.ZERO
        pendingBills.forEach { bill ->
            val monthlyFines = monthlyPendingFinesService.getMonthlyPendingFines(
                partnerId, 
                bill.billingPeriodStart.monthValue, 
                bill.billingPeriodStart.year
            )
            totalFinesDebt = totalFinesDebt.add(monthlyFines.totalFines)
        }
        
        // También incluir multas del mes actual si no hay una factura pendiente para hoy aún
        // o si queremos que siempre se vean las multas del mes en curso aunque no haya factura.
        // Pero el requerimiento dice "si es diciembre solo multas de diciembre", lo cual sugiere
        // que las multas van atadas a la factura del mes.
        
        return billsDebt.add(totalFinesDebt)
    }

    fun getDebtorsList(): List<DebtReportDto> {
        val allPartners = partnerRepository.findAllByActive(true, Sort.unsorted())
        
        return allPartners
            .map { partner ->
                val currentDebt = calculateTotalDebt(partner.id)
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
                    totalDebt = currentDebt,
                    pendingBillsCount = pendingBills.size,
                    overdueBillsCount = overdueBills.size,
                    oldestDebtDate = oldestBill?.billingPeriodStart,
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
        val pendingBills = bills.filter { it.status.code in listOf("PENDING", "PARTIAL_PAID", "OVERDUE") }
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
