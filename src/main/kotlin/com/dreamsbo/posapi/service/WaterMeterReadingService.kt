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

        // Validar que no exista ya una lectura para este socio en el mismo mes
        val existingReadings = waterMeterReadingRepository.findByPartnerIdAndYearAndMonth(
            input.partnerId,
            input.readingDate.year,
            input.readingDate.monthValue,
            true
        )
        if (existingReadings.isNotEmpty()) {
            val monthName = getMonthName(input.readingDate.monthValue)
            throw BadRequestException("Ya existe una lectura registrada para este socio en el mes de $monthName ${input.readingDate.year}. Solo se permite una lectura por mes.")
        }

        // Get previous reading
        val previousReadingEntity = waterMeterReadingRepository.findLatestByPartnerId(input.partnerId, true)
        val previousReading = previousReadingEntity.map { it.currentReading }.orElse(BigDecimal.ZERO)

        // Validate current reading is greater than previous
        if (input.currentReading < previousReading) {
            throw BadRequestException("La lectura actual (${input.currentReading}) debe ser mayor a la lectura anterior ($previousReading)")
        }

        val consumption = input.currentReading - previousReading

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
            currentReading = input.currentReading,
            consumption = consumption,
            readerUser = readerUser, // Usuario que registra la lectura (opcional)
            observation = input.observation,
            image = image
        )

        val savedReading = waterMeterReadingRepository.save(reading)
        
        // Generar automáticamente una factura para esta lectura
        try {
            println("🔄 Intentando generar factura para lectura ${savedReading.id} del socio ${partner.id}")
            val generatedBill = waterBillingService.generateBillFromReading(savedReading.id)
            println("✅ Factura generada exitosamente: ${generatedBill.billNumber} con estado ${generatedBill.statusCode}")
        } catch (e: BadRequestException) {
            // Si el socio no tiene conexión de agua, solo registrar la advertencia
            println("⚠️ No se generó factura: ${e.message}")
        } catch (e: NotFoundEntityException) {
            // Si falta el estado PENDING u otro recurso necesario, registrar el error
            println("❌ Error crítico al generar factura: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            // Cualquier otro error
            println("❌ Error inesperado al generar factura: ${e.message}")
            e.printStackTrace()
        }
        
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

        input.currentReading?.let { newReading ->
            if (newReading < reading.previousReading) {
                throw BadRequestException("La lectura actual ($newReading) debe ser mayor a la lectura anterior (${reading.previousReading})")
            }
            reading.currentReading = newReading
            reading.consumption = newReading - reading.previousReading
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
            // Usar el servicio de facturación para anular la factura
            // Esto maneja tanto facturas PENDING (las desactiva) como PAID (las marca como CANCELLED y gestiona reembolsos)
            waterBillingService.cancelBill(bill.id, userId)
        }

        // Desactivar la lectura
        reading.active = false
        waterMeterReadingRepository.save(reading)
    }

    fun validateReading(partnerId: UUID, currentReading: BigDecimal): Boolean {
        val previousReading = waterMeterReadingRepository.findLatestByPartnerId(partnerId, true)
            .map { it.currentReading }
            .orElse(BigDecimal.ZERO)

        return currentReading >= previousReading
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
