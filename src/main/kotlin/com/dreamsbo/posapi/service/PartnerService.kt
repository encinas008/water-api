package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.persistence.entity.PartnerEntity
import com.dreamsbo.posapi.persistence.repository.ConnectionStatusTypeRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import com.dreamsbo.posapi.persistence.repository.WaterBillRepository
import com.dreamsbo.posapi.persistence.repository.WaterPaymentRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class PartnerService(
    private val partnerRepository: PartnerRepository,
    private val connectionStatusTypeRepository: ConnectionStatusTypeRepository,
    private val waterBillRepository: WaterBillRepository,
    private val waterPaymentRepository: WaterPaymentRepository,
    private val debtManagementService: DebtManagementService,
    private val waterPaymentService: WaterPaymentService,
) {

    fun findAll(): List<PartnerOutputDto> {
        val partners = mutableListOf<PartnerOutputDto>()

        partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt")).forEach {
            partners.add(toPartnerOutputDto(it))
        }

        return partners
    }

    fun findAllPaginated(page: Int, size: Int, search: String?): Page<PartnerOutputDto> {
        val pageable: Pageable = if (search.isNullOrBlank()) {
            // Use multiple sort keys for deterministic results
            PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("partnerNumber")))
        } else {
            // Even with search, apply sorting to avoid ghost duplicates across pages
            PageRequest.of(page, size, Sort.by(Sort.Order.asc("partnerNumber")))
        }
        
        val partnerPage = if (search.isNullOrBlank()) {
            partnerRepository.findAllByActive(true, pageable)
        } else {
            partnerRepository.findAllByActiveAndSearch(true, search.trim(), pageable)
        }
        
        return partnerPage.map { toPartnerOutputDto(it) }
    }

    fun findById(id: UUID): PartnerOutputDto {
        val partnerEntity = partnerRepository.findById(id)
        if (partnerEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = $id")
        }
        return toPartnerOutputDto(partnerEntity.get())
    }

    @Transactional
    fun createPartner(input: PartnerInputDto): PartnerOutputDto {
        // Validar número de medidor (opcional, letras y números, máximo 50 caracteres, único)
        var waterMeterNumber: String? = null
        if (input.waterMeterNumber != null && input.waterMeterNumber.isNotBlank()) {
            val trimmedMeterNumber = input.waterMeterNumber.trim().uppercase()
            
            if (trimmedMeterNumber.length > 50) {
                throw BadRequestException("El número de medidor no puede exceder 50 caracteres")
            }
            
            // Validar que solo contenga letras y números
            if (!trimmedMeterNumber.matches(Regex("^[A-Z0-9]+$"))) {
                throw BadRequestException("El número de medidor solo puede contener letras y números")
            }
            
            // Validar que el número de medidor sea único (comparar en mayúsculas)
            val existingPartner = partnerRepository.findByWaterMeterNumberAndActive(
                trimmedMeterNumber,
                true
            )
            if (existingPartner.isPresent) {
                throw BadRequestException("El número de medidor $trimmedMeterNumber ya está registrado para otro socio")
            }
            
            waterMeterNumber = trimmedMeterNumber
        }

        var connectionStatus = input.connectionStatusCode?.let {
            connectionStatusTypeRepository.findByCodeAndActive(it, true)
                .orElseThrow { NotFoundEntityException("Estado de conexión no encontrado: $it") }
        }

        val partner = PartnerEntity(
            fullName = input.fullName.uppercase().trim(),
            partnerIdentificationNumber = input.partnerIdentificationNumber,
            cellphone = input.cellphone,
            address = input.address,
            observation = input.observation,
            waterMeterNumber = waterMeterNumber,
            connectionStatus = connectionStatus,
            connectionDate = input.connectionDate,
            waterConnectionAddress = input.waterConnectionAddress,
            isElderly = input.isElderly,
            notes = input.notes
        )

        val savedPartner = partnerRepository.save(partner)
        // Forzar flush para que PostgreSQL genere el partner_number
        partnerRepository.flush()
        // Recargar la entidad para obtener el partner_number generado
        val refreshedPartner = partnerRepository.findById(savedPartner.id).orElse(savedPartner)

        // Registrar cobro de instalación si se proporciona
        if (input.installationAmount != null && input.installationAmount > BigDecimal.ZERO) {
            val paymentInput = WaterPaymentInputDto(
                userId = input.userId ?: UUID.fromString("00000000-0000-0000-0000-000000000000"),
                waterBillId = null,
                partnerId = refreshedPartner.id,
                paymentDate = input.connectionDate ?: LocalDate.now(),
                amount = input.installationAmount,
                paymentTypeId = input.paymentTypeId ?: UUID.randomUUID(),
                cashBalanceId = input.cashBalanceId,
                observation = "COBRO POR INSTALACIÓN DE AGUA",
                includePendingFines = false
            )
            val paymentOutput = waterPaymentService.recordPayment(paymentInput)
            return toPartnerOutputDto(refreshedPartner, paymentOutput.id)
        }

        return toPartnerOutputDto(refreshedPartner)
    }

    @Transactional
    fun updatePartner(id: UUID, input: PartnerUpdateDto): PartnerOutputDto {
        val partnerEntity = partnerRepository.findById(id)
        if (partnerEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = $id")
        }

        val partner = partnerEntity.get()

        input.fullName?.let { partner.fullName = it.uppercase().trim() }
        input.partnerIdentificationNumber?.let { partner.partnerIdentificationNumber = it }
        input.cellphone?.let { partner.cellphone = it }
        input.address?.let { partner.address = it }
        input.observation?.let { partner.observation = it }
        input.waterMeterNumber?.let { meterNumber ->
            val trimmedMeterNumber = meterNumber.trim().uppercase()
            
            // Validar número de medidor (opcional, letras y números, máximo 50 caracteres)
            if (trimmedMeterNumber.isNotBlank()) {
                if (trimmedMeterNumber.length > 50) {
                    throw BadRequestException("El número de medidor no puede exceder 50 caracteres")
                }
                
                // Validar que solo contenga letras y números
                if (!trimmedMeterNumber.matches(Regex("^[A-Z0-9]+$"))) {
                    throw BadRequestException("El número de medidor solo puede contener letras y números")
                }
                
                // Validar que el número de medidor sea único (excepto para el socio actual, comparar en mayúsculas)
                val existingPartner = partnerRepository.findByWaterMeterNumberAndActive(
                    trimmedMeterNumber,
                    true
                )
                if (existingPartner.isPresent && existingPartner.get().id != partner.id) {
                    throw BadRequestException("El número de medidor $trimmedMeterNumber ya está registrado para otro socio")
                }
                partner.waterMeterNumber = trimmedMeterNumber
            } else {
                // Si se envía vacío, limpiar el campo
                partner.waterMeterNumber = null
            }
        }
        input.connectionDate?.let { partner.connectionDate = it }
        input.waterConnectionAddress?.let { partner.waterConnectionAddress = it }
        input.isElderly?.let { partner.isElderly = it }
        input.notes?.let { partner.notes = it }

        input.connectionStatusCode?.let { code ->
            // Validar: Si el socio está CORTADO, no se puede cambiar el estado manualmente si aún tiene deuda por mora
            if (partner.connectionStatus?.code == "CUT_OFF" && code != "CUT_OFF") {
                val unpaidBillsCount = waterBillRepository.countUnpaidBillsByPartnerId(partner.id)
                if (unpaidBillsCount >= 4) {
                    throw BadRequestException("No se puede cambiar el estado de un socio CORTADO manualmente hasta que regularice su deuda (mínimo menos de 4 facturas pendientes).")
                }
            }
            partner.connectionStatus = connectionStatusTypeRepository.findByCodeAndActive(code, true)
                .orElseThrow { NotFoundEntityException("Estado de conexión no encontrado: $code") }
        }

        partner.updatedAt = OffsetDateTime.now()

        val updatedPartner = partnerRepository.save(partner)
        return toPartnerOutputDto(updatedPartner)
    }

    @Transactional
    fun deactivatePartner(id: UUID) {
        val partnerEntity = partnerRepository.findById(id)
        if (partnerEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = $id")
        }

        val partner = partnerEntity.get()
        partner.active = false
        partner.updatedAt = OffsetDateTime.now()
        partnerRepository.save(partner)
    }

    @Transactional
    fun updateConnectionStatus(id: UUID, input: ConnectionStatusUpdateDto): PartnerOutputDto {
        val partnerEntity = partnerRepository.findById(id)
        if (partnerEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = $id")
        }

        val partner = partnerEntity.get()
        
        // Validar: Si el socio está CORTADO, no se puede cambiar el estado manualmente si aún tiene deuda por mora
        if (partner.connectionStatus?.code == "CUT_OFF" && input.connectionStatusCode != "CUT_OFF") {
            val unpaidBillsCount = waterBillRepository.countUnpaidBillsByPartnerId(partner.id)
            if (unpaidBillsCount >= 4) {
                throw BadRequestException("No se puede cambiar el estado de un socio CORTADO manualmente hasta que regularice su deuda (mínimo menos de 4 facturas pendientes).")
            }
        }

        partner.connectionStatus = connectionStatusTypeRepository.findByCodeAndActive(input.connectionStatusCode, true)
            .orElseThrow { NotFoundEntityException("Estado de conexión no encontrado: ${input.connectionStatusCode}") }

        partner.updatedAt = OffsetDateTime.now()

        val updatedPartner = partnerRepository.save(partner)
        return toPartnerOutputDto(updatedPartner)
    }

    fun getPartnerWithDebtSummary(id: UUID): PartnerDebtSummaryDto {
        val partnerEntity = partnerRepository.findById(id)
        if (partnerEntity.isEmpty) {
            throw NotFoundEntityException("No se ha encontrado el socio. PartnerId = $id")
        }

        val partner = partnerEntity.get()
        val currentDebt = debtManagementService.calculateTotalDebt(partner.id)
        val bills = waterBillRepository.findByPartnerIdAndActive(id, true, Sort.unsorted())

        val pendingBills = bills.filter { it.status.code in listOf("PENDING", "PARTIAL_PAID", "OVERDUE") }
        val overdueBills =
            bills.filter { it.status.code == "OVERDUE" || (it.dueDate.isBefore(LocalDate.now()) && it.status.code != "PAID") }
        val totalPendingAmount = pendingBills.sumOf { it.remainingBalance }

        val payments =
            waterPaymentRepository.findByPartnerIdAndActive(id, true, Sort.by(Sort.Direction.DESC, "paymentDate"))
        val lastPaymentDate = payments.firstOrNull()?.paymentDate

        return PartnerDebtSummaryDto(
            partnerId = partner.id,
            partnerName = partner.fullName,
            currentDebt = currentDebt,
            pendingBills = pendingBills.size,
            overdueBills = overdueBills.size,
            totalPendingAmount = totalPendingAmount,
            lastPaymentDate = lastPaymentDate,
            lastBillingDate = partner.lastBillingDate,
            connectionStatus = partner.connectionStatus?.name
        )
    }

    fun searchPartners(query: String): List<PartnerOutputDto> {
        return partnerRepository.searchByTerm(query.trim()).map { toPartnerOutputDto(it) }
    }

    fun checkWaterMeterNumberExists(waterMeterNumber: String, excludePartnerId: UUID? = null): Boolean {
        if (waterMeterNumber.isBlank()) {
            return false
        }
        // Convertir a mayúsculas para comparación
        val upperMeterNumber = waterMeterNumber.trim().uppercase()
        val existingPartner = partnerRepository.findByWaterMeterNumberAndActive(
            upperMeterNumber,
            true
        )
        return existingPartner.isPresent && (excludePartnerId == null || existingPartner.get().id != excludePartnerId)
    }

    private fun toPartnerOutputDto(entity: PartnerEntity, lastPaymentId: UUID? = null): PartnerOutputDto {
        return PartnerOutputDto(
            id = entity.id,
            partnerNumber = entity.partnerNumber,
            fullName = entity.fullName,
            partnerIdentificationNumber = entity.partnerIdentificationNumber,
            cel = entity.cellphone,
            address = entity.address,
            waterMeterNumber = entity.waterMeterNumber,
            connectionStatusCode = entity.connectionStatus?.code,
            connectionStatusName = entity.connectionStatus?.name,
            connectionDate = entity.connectionDate,
            waterConnectionAddress = entity.waterConnectionAddress,
            currentDebt = debtManagementService.calculateTotalDebt(entity.id),
            lastBillingDate = entity.lastBillingDate,
            isElderly = entity.isElderly,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            active = entity.active,
            lastPaymentId = lastPaymentId
        )
    }
}
