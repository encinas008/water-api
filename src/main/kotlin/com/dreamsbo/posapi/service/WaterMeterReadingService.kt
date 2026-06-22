package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.WaterMeterReadingInputDto
import com.dreamsbo.posapi.dto.WaterMeterReadingOutputDto
import com.dreamsbo.posapi.dto.WaterMeterReadingUpdateDto
import com.dreamsbo.posapi.persistence.entity.WaterMeterReadingEntity
import com.dreamsbo.posapi.persistence.repository.*
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

@Service
class WaterMeterReadingService(
    private val waterMeterReadingRepository: WaterMeterReadingRepository,
    private val partnerRepository: PartnerRepository,
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository,
    private val waterBillingService: WaterBillingService,
    private val waterBillRepository: WaterBillRepository,
    private val billConceptItemRepository: BillConceptItemRepository,
) {

    @Transactional
    fun recordReading(input: WaterMeterReadingInputDto): WaterMeterReadingOutputDto {
        val partner = partnerRepository.findById(input.partnerId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el socio. PartnerId = ${input.partnerId}") }

        // Validar que no exista ya una lectura ACTIVA para este socio en el mismo mes
        val existingReadings = waterMeterReadingRepository.findByPartnerIdAndYearAndMonth(
            input.partnerId,
            input.readingDate.year,
            input.readingDate.monthValue,
            true
        )
        if (existingReadings.isNotEmpty()) {
            val monthName = getMonthName(input.readingDate.monthValue)
            val existingDate = existingReadings.first().readingDate
            throw BadRequestException("Ya existe una lectura activa para este socio en el mes de $monthName ${input.readingDate.year} (fecha registrada: $existingDate). Elimínela primero antes de registrar una nueva.")
        }

        // Get previous reading: the latest reading BEFORE the input date
        // This ensures backfilling an older month uses the correct prior reading
        // e.g. registering April uses March's reading, not May's
        val previousReadingEntity = waterMeterReadingRepository.findLatestByPartnerIdBeforeDate(
            input.partnerId, true, input.readingDate
        )
        val previousReading = previousReadingEntity.map { it.currentReading }.orElse(BigDecimal.ZERO)

        // Si el usuario marca 0, se busca la última lectura NO-CERO anterior a la fecha
        val currentReading = if (input.currentReading.compareTo(BigDecimal.ZERO) == 0) {
            waterMeterReadingRepository.findLatestNonZeroByPartnerIdBeforeDate(
                input.partnerId, true, input.readingDate
            )
                .map { it.currentReading }
                .orElse(previousReading)
        } else {
            input.currentReading
        }

        // Validate current reading is greater than or equal to previous
        if (currentReading < previousReading) {
            throw BadRequestException("La lectura actual ($currentReading) debe ser mayor o igual a la lectura anterior ($previousReading)")
        }

        val consumption = currentReading - previousReading

        val image = input.imageId?.let {
            imageRepository.findById(it)
                .orElseThrow { NotFoundEntityException("No se ha encontrado la imagen. ImageId = $it") }
        }

        // Obtener el usuario si se proporciona userId
        val readerUser = input.userId?.let { userId ->
            userRepository.findById(userId)
                .orElseThrow { NotFoundEntityException("No se ha encontrado el usuario. UserId = $userId") }
        }

        val reading = WaterMeterReadingEntity(
            partner = partner,
            readingDate = input.readingDate,
            previousReading = previousReading,
            currentReading = currentReading,
            consumption = consumption,
            readerUser = readerUser, // Usuario que registra la lectura (opcional)
            observation = input.observation,
            image = image
        )

        val savedReading = waterMeterReadingRepository.save(reading)
        
        // Generación automática de factura desactivada según nuevo requerimiento. 
        // Las facturas ahora se generan manualmente (por lote) a través de la UI.
        
        return toWaterMeterReadingOutputDto(savedReading)
    }

    fun getReadingHistory(partnerId: UUID): List<WaterMeterReadingOutputDto> {
        val readings = waterMeterReadingRepository.findByPartnerIdAndActive(
            partnerId,
            true,
            Sort.by(Sort.Direction.DESC, "readingDate")
        )
        return readings.map { toWaterMeterReadingOutputDto(it) }
    }

    fun getReadingsByPeriod(startDate: LocalDate, endDate: LocalDate): List<WaterMeterReadingOutputDto> {
        val readings = waterMeterReadingRepository.findByReadingDateBetweenAndActive(startDate, endDate, true)
        return readings.map { toWaterMeterReadingOutputDto(it) }
    }

    fun findAllPaginated(page: Int, size: Int, search: String?): Page<WaterMeterReadingOutputDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "readingDate"))
        
        val readingPage = if (search.isNullOrBlank()) {
            waterMeterReadingRepository.findAllByActive(true, pageable)
        } else {
            waterMeterReadingRepository.findAllByActiveAndSearch(true, search.trim(), pageable)
        }
        
        return readingPage.map { toWaterMeterReadingOutputDto(it) }
    }

    fun getReadingById(id: UUID): WaterMeterReadingOutputDto {
        val reading = waterMeterReadingRepository.findById(id)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la lectura. ReadingId = $id") }
        return toWaterMeterReadingOutputDto(reading)
    }

    @Transactional
    fun updateReading(id: UUID, input: WaterMeterReadingUpdateDto): WaterMeterReadingOutputDto {
        val reading = waterMeterReadingRepository.findById(id)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la lectura. ReadingId = $id") }

        input.readingDate?.let { reading.readingDate = it }
        input.observation?.let { reading.observation = it }

        input.currentReading?.let { newReadingValue ->
            val finalReading = if (newReadingValue.compareTo(BigDecimal.ZERO) == 0) {
                waterMeterReadingRepository.findLatestNonZeroByPartnerId(reading.partner.id, true)
                    .map { it.currentReading }
                    .orElse(reading.previousReading)
            } else {
                newReadingValue
            }

            if (finalReading < reading.previousReading) {
                throw BadRequestException("La lectura actual ($finalReading) debe ser mayor o igual a la lectura anterior (${reading.previousReading})")
            }
            reading.currentReading = finalReading
            reading.consumption = finalReading - reading.previousReading
        }

        reading.updatedAt = OffsetDateTime.now()

        val updatedReading = waterMeterReadingRepository.save(reading)
        return toWaterMeterReadingOutputDto(updatedReading)
    }

    @Transactional
    fun deleteReading(id: UUID, userId: UUID) {
        val reading = waterMeterReadingRepository.findById(id)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la lectura. ReadingId = $id") }

        // Buscar si existe una factura activa para esta lectura
        val bill = waterBillRepository.findAll().firstOrNull { it.reading?.id == id && it.active }
        
        if (bill != null) {
            // ANULACIÓN DIRECTA (sin clonar) — solo desde eliminación de lectura
            val cancelledStatus = waterBillingService.findBillStatus("CANCELLED")
            
            // Desactivar pagos asociados
            val payments = waterBillingService.findPaymentsByBill(bill.id)
            payments.forEach { payment ->
                payment.active = false
                waterBillingService.savePayment(payment)
                
                // Desactivar detalles del pago
                val details = waterBillingService.findPaymentDetailsByPayment(payment.id)
                details.forEach { it.active = false }
                waterBillingService.savePaymentDetails(details)
            }
            
            // Desactivar conceptos de la factura
            val concepts = billConceptItemRepository.findByWaterBillIdAndActive(bill.id, true)
            concepts.forEach { it.active = false }
            billConceptItemRepository.saveAll(concepts)
            
            // Restar la deuda pendiente del socio (lo que quedaba por pagar)
            val partner = bill.partner
            partner.currentDebt = partner.currentDebt.subtract(bill.remainingBalance)
            if (partner.currentDebt < BigDecimal.ZERO) {
                partner.currentDebt = BigDecimal.ZERO
            }
            partnerRepository.save(partner)
            
            // Marcar factura como CANCELLED
            bill.status = cancelledStatus
            bill.active = false
            bill.updatedAt = OffsetDateTime.now()
            waterBillRepository.save(bill)
        }

        // Desactivar la lectura
        reading.active = false
        waterMeterReadingRepository.save(reading)
    }

    fun validateReading(partnerId: UUID, currentReading: BigDecimal): Boolean {
        val previousReading = waterMeterReadingRepository.findLatestByPartnerId(partnerId, true)
            .map { it.currentReading }
            .orElse(BigDecimal.ZERO)

        val finalReading = if (currentReading.compareTo(BigDecimal.ZERO) == 0) {
            waterMeterReadingRepository.findLatestNonZeroByPartnerId(partnerId, true)
                .map { it.currentReading }
                .orElse(previousReading)
        } else {
            currentReading
        }

        return finalReading >= previousReading
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

    private fun toWaterMeterReadingOutputDto(entity: WaterMeterReadingEntity): WaterMeterReadingOutputDto {
        return WaterMeterReadingOutputDto(
            id = entity.id,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            partnerNumber = entity.partner.partnerNumber,
            readingDate = entity.readingDate,
            previousReading = entity.previousReading,
            currentReading = entity.currentReading,
            consumption = entity.consumption,
            readerUserName = entity.readerUser?.let { "${it.profile.name} ${it.profile.lastname}" } ?: "",
            observation = entity.observation,
            imageUrl = entity.image?.url,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
