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
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Service
class PartnerService(
    private val partnerRepository: PartnerRepository,
    private val connectionStatusTypeRepository: ConnectionStatusTypeRepository,
    private val waterBillRepository: WaterBillRepository,
    private val waterPaymentRepository: WaterPaymentRepository,
) {

    fun findAll(): List<PartnerOutputDto> {
        val partners = mutableListOf<PartnerOutputDto>()

        partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt")).forEach {
            partners.add(toPartnerOutputDto(it))
        }

        return partners
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
        // Validar número de medidor (obligatorio, mínimo 6 dígitos, único)
        if (input.waterMeterNumber == null || input.waterMeterNumber.isBlank()) {
            throw BadRequestException("El número de medidor es obligatorio")
        }
        
        val trimmedMeterNumber = input.waterMeterNumber.trim()
        if (trimmedMeterNumber.length < 6) {
            throw BadRequestException("El número de medidor debe tener al menos 6 dígitos")
        }
        
        if (trimmedMeterNumber.length > 50) {
            throw BadRequestException("El número de medidor no puede exceder 50 caracteres")
        }
        
        // Validar que solo contenga números
        if (!trimmedMeterNumber.matches(Regex("^[0-9]+$"))) {
            throw BadRequestException("El número de medidor solo puede contener números")
        }
        
        // Validar que el número de medidor sea único
        val existingPartner = partnerRepository.findByWaterMeterNumberAndActive(
            trimmedMeterNumber,
            true
        )
        if (existingPartner.isPresent) {
            throw BadRequestException("El número de medidor $trimmedMeterNumber ya está registrado para otro socio")
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
            waterMeterNumber = input.waterMeterNumber,
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
        input.waterConnectionNumber?.let { partner.waterConnectionNumber = it }
        input.waterMeterNumber?.let { meterNumber ->
            val trimmedMeterNumber = meterNumber.trim()
            
            // Validar que el número de medidor tenga al menos 6 dígitos
            if (trimmedMeterNumber.isNotBlank()) {
                if (trimmedMeterNumber.length < 6) {
                    throw BadRequestException("El número de medidor debe tener al menos 6 dígitos")
                }
                
                if (trimmedMeterNumber.length > 50) {
                    throw BadRequestException("El número de medidor no puede exceder 50 caracteres")
                }
                
                // Validar que solo contenga números
                if (!trimmedMeterNumber.matches(Regex("^[0-9]+$"))) {
                    throw BadRequestException("El número de medidor solo puede contener números")
                }
                
                // Validar que el número de medidor sea único (excepto para el socio actual)
                val existingPartner = partnerRepository.findByWaterMeterNumberAndActive(
                    trimmedMeterNumber,
                    true
                )
                if (existingPartner.isPresent && existingPartner.get().id != partner.id) {
                    throw BadRequestException("El número de medidor $trimmedMeterNumber ya está registrado para otro socio")
                }
            }
            partner.waterMeterNumber = trimmedMeterNumber
        }
        input.connectionDate?.let { partner.connectionDate = it }
        input.waterConnectionAddress?.let { partner.waterConnectionAddress = it }
        input.isElderly?.let { partner.isElderly = it }
        input.notes?.let { partner.notes = it }

        input.connectionStatusCode?.let { code ->
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
            waterConnectionNumber = partner.waterConnectionNumber,
            currentDebt = partner.currentDebt,
            pendingBills = pendingBills.size,
            overdueBills = overdueBills.size,
            totalPendingAmount = totalPendingAmount,
            lastPaymentDate = lastPaymentDate,
            lastBillingDate = partner.lastBillingDate,
            connectionStatus = partner.connectionStatus?.name
        )
    }

    fun searchPartners(query: String): List<PartnerOutputDto> {
        val allPartners = partnerRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt"))

        val filteredPartners = allPartners.filter {
            it.fullName.contains(query, ignoreCase = true) ||
                    it.partnerIdentificationNumber?.contains(query, ignoreCase = true) == true ||
                    it.waterConnectionNumber?.contains(query, ignoreCase = true) == true
        }

        return filteredPartners.map { toPartnerOutputDto(it) }
    }

    fun checkWaterMeterNumberExists(waterMeterNumber: String, excludePartnerId: UUID? = null): Boolean {
        if (waterMeterNumber.isBlank()) {
            return false
        }
        val existingPartner = partnerRepository.findByWaterMeterNumberAndActive(
            waterMeterNumber.trim(),
            true
        )
        return existingPartner.isPresent && (excludePartnerId == null || existingPartner.get().id != excludePartnerId)
    }

    private fun toPartnerOutputDto(entity: PartnerEntity): PartnerOutputDto {
        return PartnerOutputDto(
            id = entity.id,
            partnerNumber = entity.partnerNumber,
            fullName = entity.fullName,
            partnerIdentificationNumber = entity.partnerIdentificationNumber,
            cel = entity.cellphone,
            address = entity.address,
            waterConnectionNumber = entity.waterConnectionNumber,
            waterMeterNumber = entity.waterMeterNumber,
            connectionStatusCode = entity.connectionStatus?.code,
            connectionStatusName = entity.connectionStatus?.name,
            connectionDate = entity.connectionDate,
            waterConnectionAddress = entity.waterConnectionAddress,
            currentDebt = entity.currentDebt,
            lastBillingDate = entity.lastBillingDate,
            isElderly = entity.isElderly,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            active = entity.active
        )
    }
}
