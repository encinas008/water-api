package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.BillConceptItemDto
import com.dreamsbo.posapi.dto.PendingFineDto
import com.dreamsbo.posapi.dto.WaterBillDetailDto
import com.dreamsbo.posapi.dto.WaterBillGenerationDto
import com.dreamsbo.posapi.dto.WaterBillInputDto
import com.dreamsbo.posapi.dto.WaterBillOutputDto
import com.dreamsbo.posapi.dto.WaterBillStatsDto
import com.dreamsbo.posapi.dto.WaterBillSummaryDto
import com.dreamsbo.posapi.persistence.entity.BillConceptItemEntity
import com.dreamsbo.posapi.persistence.entity.WaterBillEntity
import com.dreamsbo.posapi.persistence.repository.*
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class WaterBillingService(
    private val waterBillRepository: WaterBillRepository,
    private val partnerRepository: PartnerRepository,
    private val waterMeterReadingRepository: WaterMeterReadingRepository,
    private val billStatusTypeRepository: BillStatusTypeRepository,
    private val billConceptItemRepository: BillConceptItemRepository,
    private val waterPaymentRepository: WaterPaymentRepository,
    private val waterPaymentService: com.dreamsbo.posapi.service.WaterPaymentService,
    private val waterPaymentDetailRepository: com.dreamsbo.posapi.persistence.repository.WaterPaymentDetailRepository,
    private val monthlyPendingFinesService: MonthlyPendingFinesService,
) {

    @Transactional
    fun generateMonthlyBills(input: WaterBillGenerationDto): List<WaterBillOutputDto> {
        val pendingStatus = billStatusTypeRepository.findByCodeAndActive("PENDING", true)
            .orElseThrow { NotFoundEntityException("Estado de factura PENDING no encontrado") }

        val partners = if (input.partnerIds != null && input.partnerIds.isNotEmpty()) {
            input.partnerIds.mapNotNull { partnerRepository.findById(it).orElse(null) }
        } else {
            partnerRepository.findAllByActive(true, Sort.unsorted())
        }

        val generatedBills = mutableListOf<WaterBillEntity>()

        partners.forEach { partner ->
            // Only generate bills for partners with water connection
                val latestReading = waterMeterReadingRepository.findLatestByPartnerId(partner.id, true)

                val consumption = latestReading.map { it.consumption }.orElse(BigDecimal.ZERO)
                val baseAmount = consumption * input.ratePerM3

                val billNumber = generateBillNumber(partner.id)

                val bill = WaterBillEntity(
                    billNumber = billNumber,
                    partner = partner,
                    reading = latestReading.orElse(null),
                    billingPeriodStart = input.billingPeriodStart,
                    billingPeriodEnd = input.billingPeriodEnd,
                    consumptionM3 = consumption,
                    ratePerM3 = input.ratePerM3,
                    baseAmount = baseAmount,
                    totalAmount = baseAmount,  // Temporal, se actualizará con conceptos
                    remainingBalance = baseAmount,  // Temporal
                    status = pendingStatus,
                    dueDate = input.dueDate
                )

                val savedBill = waterBillRepository.save(bill)

                // Crear conceptos de cobro por defecto y calcular total
                val totalFromConcepts = createDefaultBillConcepts(savedBill, input.billingPeriodStart)

                // Actualizar total de la factura basado en conceptos
                savedBill.totalAmount = totalFromConcepts
                savedBill.remainingBalance = totalFromConcepts
                waterBillRepository.save(savedBill)

                generatedBills.add(savedBill)

                // Update partner's last billing date and debt
                partner.lastBillingDate = LocalDate.now()
                partner.currentDebt = partner.currentDebt.add(savedBill.totalAmount)
                partnerRepository.save(partner)
            }

        return generatedBills.map { toWaterBillOutputDto(it) }
    }

    fun calculateBillAmount(consumption: BigDecimal, ratePerM3: BigDecimal): BigDecimal {
        return consumption * ratePerM3
    }

    fun getBillsByPartner(partnerId: UUID): List<WaterBillOutputDto> {
        val bills = waterBillRepository.findByPartnerIdAndActive(
            partnerId,
            true,
            Sort.by(Sort.Direction.DESC, "billingPeriodStart")
        )
        return bills.map { toWaterBillOutputDto(it) }
    }

    fun getPendingBills(): List<WaterBillOutputDto> {
        val bills = waterBillRepository.findByStatusCodesAndActive(listOf("PENDING", "PARTIAL_PAID"), true)
        return bills.map { toWaterBillOutputDto(it) }
    }

    fun getOverdueBills(): List<WaterBillOutputDto> {
        val bills = waterBillRepository.findOverdueBills(LocalDate.now(), true)
        return bills.map { toWaterBillOutputDto(it) }
    }

    fun getBillDetails(id: UUID): WaterBillOutputDto {
        val bill = waterBillRepository.findById(id)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la factura. BillId = $id") }
        return toWaterBillOutputDto(bill)
    }

    fun getBillDetailWithPayments(id: UUID): WaterBillDetailDto {
        val bill = waterBillRepository.findById(id)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la factura. BillId = $id") }
        
        val billDto = toWaterBillOutputDto(bill)
        
        // Obtener pagos relacionados a esta factura
        val payments = waterPaymentService.getPaymentsByBillId(id)
        
        return WaterBillDetailDto(
            bill = billDto,
            payments = payments
        )
    }

    fun getBillSummaries(): List<WaterBillSummaryDto> {
        val bills = waterBillRepository.findAll()
        return bills.map { toWaterBillSummaryDto(it) }
    }

    fun getAllBills(): List<WaterBillOutputDto> {
        val bills = waterBillRepository.findAll()
        return bills.map { toWaterBillOutputDto(it) }
    }

    fun getBillStats(): WaterBillStatsDto {
        val stats = waterBillRepository.getBillStats(LocalDate.now(), true).firstOrNull()

        return if (stats != null) {
            WaterBillStatsDto(
                totalBills = stats[0] as Long,
                pendingBillsCount = stats[1] as Long,
                overdueBillsCount = stats[2] as Long,
                paidBillsCount = stats[3] as Long,
                totalPendingAmount = stats[4] as BigDecimal? ?: BigDecimal.ZERO,
                totalOverdueAmount = stats[5] as BigDecimal? ?: BigDecimal.ZERO,
                totalPaidAmount = stats[6] as BigDecimal? ?: BigDecimal.ZERO
            )
        } else {
            WaterBillStatsDto(
                totalBills = 0,
                pendingBillsCount = 0,
                overdueBillsCount = 0,
                paidBillsCount = 0,
                totalPendingAmount = BigDecimal.ZERO,
                totalOverdueAmount = BigDecimal.ZERO,
                totalPaidAmount = BigDecimal.ZERO
            )
        }
    }

    fun findAllPaginated(page: Int, size: Int, search: String?, statusCode: String?): Page<WaterBillOutputDto> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        
        val billPage = when {
            !search.isNullOrBlank() && !statusCode.isNullOrBlank() -> {
                waterBillRepository.findAllByActiveAndStatusAndSearch(true, statusCode.trim(), search.trim(), pageable)
            }
            !statusCode.isNullOrBlank() -> {
                waterBillRepository.findAllByActiveAndStatus(true, statusCode.trim(), pageable)
            }
            !search.isNullOrBlank() -> {
                waterBillRepository.findAllByActiveAndSearch(true, search.trim(), pageable)
            }
            else -> {
                waterBillRepository.findAllByActive(true, pageable)
            }
        }
        
        return billPage.map { toWaterBillOutputDto(it) }
    }

    /**
     * Genera automáticamente una factura cuando se registra una lectura del mes.
     * Calcula el período de facturación basado en la fecha de lectura y genera la factura con conceptos por defecto.
     */
    @Transactional
    fun generateBillFromReading(readingId: UUID, defaultRatePerM3: BigDecimal = BigDecimal("2.50")): WaterBillOutputDto {
        println("📋 generateBillFromReading llamado para readingId: $readingId")
        
        val reading = waterMeterReadingRepository.findById(readingId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la lectura. ReadingId = $readingId") }

        val partner = reading.partner

        // Verificar si ya existe una factura para esta lectura
        val existingBill = waterBillRepository.findAll().firstOrNull { it.reading?.id == readingId && it.active }
        if (existingBill != null) {
            println("ℹ️ Ya existe una factura para esta lectura: ${existingBill.billNumber}")
            return toWaterBillOutputDto(existingBill)
        }

        println("🔍 Buscando estado PENDING...")
        val pendingStatus = billStatusTypeRepository.findByCodeAndActive("PENDING", true)
            .orElseGet {
                // Listar todos los estados disponibles para debugging
                val allStatuses = billStatusTypeRepository.findAll()
                println("❌ ERROR: Estado PENDING no encontrado en la base de datos")
                println("📊 Estados disponibles en BD: ${allStatuses.map { "${it.code} (${it.name})" }}")
                throw NotFoundEntityException("Estado de factura PENDING no encontrado. Estados disponibles: ${allStatuses.map { it.code }}")
            }
        println("✅ Estado PENDING encontrado: ${pendingStatus.name}")

        // Calcular período de facturación basado en la fecha de lectura
        val readingDate = reading.readingDate
        val billingPeriodStart = readingDate.withDayOfMonth(1) // Primer día del mes
        val billingPeriodEnd = readingDate.withDayOfMonth(readingDate.lengthOfMonth()) // Último día del mes
        
        // Calcular fecha de vencimiento (15 días después de la fecha de lectura)
        val dueDate = readingDate.plusDays(15)

        val consumption = reading.consumption
        val baseAmount = consumption * defaultRatePerM3

        val billNumber = generateBillNumber(partner.id)

        val bill = WaterBillEntity(
            billNumber = billNumber,
            partner = partner,
            reading = reading,
            billingPeriodStart = billingPeriodStart,
            billingPeriodEnd = billingPeriodEnd,
            consumptionM3 = consumption,
            ratePerM3 = defaultRatePerM3,
            baseAmount = baseAmount,
            totalAmount = baseAmount,  // Temporal, se actualizará con conceptos
            remainingBalance = baseAmount,  // Temporal
            status = pendingStatus,
            dueDate = dueDate
        )

        println("💾 Guardando factura en base de datos...")
        val savedBill = waterBillRepository.save(bill)
        println("✅ Factura guardada con ID: ${savedBill.id}, Número: ${savedBill.billNumber}")
        
        // Crear conceptos de cobro por defecto y calcular total
        println("📝 Creando conceptos de cobro...")
        val totalFromConcepts = createDefaultBillConcepts(savedBill, billingPeriodStart)
        println("✅ Conceptos creados. Total: $totalFromConcepts")
        
        // Actualizar total de la factura basado en conceptos
        savedBill.totalAmount = totalFromConcepts
        savedBill.remainingBalance = totalFromConcepts
        val finalBill = waterBillRepository.save(savedBill)
        println("✅ Factura actualizada con total: ${finalBill.totalAmount}")

        // Update partner's last billing date and debt
        println("👤 Actualizando información del socio...")
        partner.lastBillingDate = LocalDate.now()
        val previousDebt = partner.currentDebt
        partner.currentDebt = partner.currentDebt.add(finalBill.totalAmount)
        partnerRepository.save(partner)
        println("✅ Socio actualizado. Deuda anterior: $previousDebt, Nueva deuda: ${partner.currentDebt}")

        return toWaterBillOutputDto(finalBill)
    }

    /**
     * Crea una factura manualmente asociada a un socio y opcionalmente a una lectura.
     * La factura se crea con estado PENDING.
     */
    @Transactional
    fun createBill(input: WaterBillInputDto): WaterBillOutputDto {
        println("📋 createBill llamado para partnerId: ${input.partnerId}, readingId: ${input.readingId}")
        
        // Validar que el socio existe
        val partner = partnerRepository.findById(input.partnerId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el socio. PartnerId = ${input.partnerId}") }
        
        // Validar lectura si se proporciona
        val reading = input.readingId?.let { readingId ->
            val foundReading = waterMeterReadingRepository.findById(readingId)
                .orElseThrow { NotFoundEntityException("No se ha encontrado la lectura. ReadingId = $readingId") }
            
            // Validar que la lectura pertenece al socio
            if (foundReading.partner.id != input.partnerId) {
                throw BadRequestException("La lectura no pertenece al socio especificado")
            }
            
            println("📖 Lectura encontrada: ${foundReading.id}, Consumo: ${foundReading.consumption}")
            foundReading
        }
        
        // Obtener estado PENDING
        println("🔍 Buscando estado PENDING...")
        val pendingStatus = billStatusTypeRepository.findByCodeAndActive("PENDING", true)
            .orElseGet {
                val allStatuses = billStatusTypeRepository.findAll()
                println("❌ ERROR: Estado PENDING no encontrado en la base de datos")
                println("📊 Estados disponibles en BD: ${allStatuses.map { "${it.code} (${it.name})" }}")
                throw NotFoundEntityException("Estado de factura PENDING no encontrado. Estados disponibles: ${allStatuses.map { it.code }}")
            }
        println("✅ Estado PENDING encontrado: ${pendingStatus.name}")
        
        // Calcular monto base
        val baseAmount = input.consumptionM3 * input.ratePerM3
        
        // Generar número de factura
        val billNumber = generateBillNumber(partner.id)
        
        // Crear la factura
        val bill = WaterBillEntity(
            billNumber = billNumber,
            partner = partner,
            reading = reading,
            billingPeriodStart = input.billingPeriodStart,
            billingPeriodEnd = input.billingPeriodEnd,
            consumptionM3 = input.consumptionM3,
            ratePerM3 = input.ratePerM3,
            baseAmount = baseAmount,
            totalAmount = baseAmount,  // Temporal, se actualizará con conceptos
            remainingBalance = baseAmount,  // Temporal
            status = pendingStatus,
            dueDate = input.dueDate
        )
        
        println("💾 Guardando factura en base de datos...")
        val savedBill = waterBillRepository.save(bill)
        println("✅ Factura guardada con ID: ${savedBill.id}, Número: ${savedBill.billNumber}")
        
        // Crear conceptos de cobro por defecto y calcular total
        println("📝 Creando conceptos de cobro...")
        val totalFromConcepts = createDefaultBillConcepts(savedBill, input.billingPeriodStart)
        println("✅ Conceptos creados. Total: $totalFromConcepts")
        
        // Actualizar total de la factura basado en conceptos
        savedBill.totalAmount = totalFromConcepts
        savedBill.remainingBalance = totalFromConcepts
        val finalBill = waterBillRepository.save(savedBill)
        println("✅ Factura actualizada con total: ${finalBill.totalAmount}")
        
        // Actualizar información del socio (Deuda Factura + Multas del mes)
        println("👤 Actualizando información del socio...")
        partner.lastBillingDate = LocalDate.now()
        val previousDebt = partner.currentDebt
        
        // Obtener multas para incluirlas en la deuda del socio
        val currentFines = monthlyPendingFinesService.getCurrentMonthPendingFines(partner.id)
        
        // La nueva deuda es: Deuda Anterior + Total Factura + Multas actuales
        partner.currentDebt = partner.currentDebt.add(finalBill.totalAmount).add(currentFines.totalFines)
        partnerRepository.save(partner)
        println("✅ Socio actualizado. Deuda anterior: $previousDebt, Multas añadidas: ${currentFines.totalFines}, Nueva deuda: ${partner.currentDebt}")
        
        return toWaterBillOutputDto(finalBill)
    }

    private fun generateBillNumber(partnerId: UUID): String {
        val timestamp = System.currentTimeMillis()
        val partnerIdShort = partnerId.toString().substring(0, 8)
        return "WB-$partnerIdShort-$timestamp"
    }

    private fun createDefaultBillConcepts(bill: WaterBillEntity, assignedDate: LocalDate): BigDecimal {
        // Conceptos por defecto según la imagen:
        // - Consumo de agua: baseAmount (consumo * tarifa)
        // - Aporte al deporte: 2.0 BS
        // - Tarifa básica: 15.0 BS
        // - Aporte a la OTB: 3.0 BS
        
        // Crear concepto para el consumo de agua
        val consumptionConcept = BillConceptItemEntity(
            waterBill = bill,
            conceptName = "Consumo de agua (${bill.consumptionM3} m³ × ${bill.ratePerM3} Bs/m³)",
            assignedDate = assignedDate,
            amount = bill.baseAmount
        )
        
        // Conceptos adicionales fijos
        val additionalConcepts = listOf(
            BillConceptItemEntity(
                waterBill = bill,
                conceptName = "Aporte al deporte",
                assignedDate = assignedDate,
                amount = BigDecimal("2.0")
            ),
            BillConceptItemEntity(
                waterBill = bill,
                conceptName = "Tarifa básica",
                assignedDate = assignedDate,
                amount = BigDecimal("15.0")
            ),
            BillConceptItemEntity(
                waterBill = bill,
                conceptName = "Aporte a la OTB",
                assignedDate = assignedDate,
                amount = BigDecimal("3.0")
            )
        )
        
        // Guardar todos los conceptos (consumo + adicionales)
        val allConcepts = listOf(consumptionConcept) + additionalConcepts
        billConceptItemRepository.saveAll(allConcepts)
        
        // Retornar total: baseAmount (consumo) + conceptos adicionales
        val additionalTotal = additionalConcepts.sumOf { it.amount }
        return bill.baseAmount.plus(additionalTotal)
    }

    private fun toWaterBillOutputDto(entity: WaterBillEntity): WaterBillOutputDto {
        val isOverdue = entity.dueDate.isBefore(LocalDate.now()) && entity.status.code != "PAID"
        
        // Obtener conceptos de la factura
        val concepts = billConceptItemRepository.findByWaterBillIdAndActive(entity.id, true)
            .map { concept ->
                BillConceptItemDto(
                    id = concept.id,
                    conceptName = concept.conceptName,
                    assignedDate = concept.assignedDate,
                    amount = concept.amount
                )
            }
        
        // Calcular paidAmount desde water_payment
        // Sumar solo la parte de factura de cada pago (excluyendo multas)
        val payments = waterPaymentRepository.findByWaterBillIdAndActive(entity.id, true)
        var totalPaidAmount = BigDecimal.ZERO
        var totalFinesPaid = BigDecimal.ZERO
        
        payments.forEach { payment ->
            // Obtener detalles del pago para separar factura de multas
            val paymentDetails = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(payment.id, true)
            if (paymentDetails.isNotEmpty()) {
                // Si hay detalles, significa que hay multas - usar solo billAmount
                val finesAmount = paymentDetails.sumOf { it.fineAmount }
                val billAmount = payment.amount.subtract(finesAmount)
                totalPaidAmount = totalPaidAmount.add(billAmount)
                totalFinesPaid = totalFinesPaid.add(finesAmount)
            } else {
                // Si no hay detalles, todo el monto es de factura
                totalPaidAmount = totalPaidAmount.add(payment.amount)
            }
        }
        
        // El totalAmount es el total de la factura (desde entity.totalAmount)
        // El paidAmount es solo la parte de factura pagada (sin multas)
        // El remainingBalance no puede ser negativo (mínimo 0)
        val calculatedRemainingBalance = if (entity.totalAmount.subtract(totalPaidAmount) < BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else {
            entity.totalAmount.subtract(totalPaidAmount)
        }
        
        // Obtener multas pendientes si el socio no ha pagado totalmente
        var pendingFines: List<PendingFineDto> = emptyList()
        var totalFinesAmount = BigDecimal.ZERO
        
        if (entity.status.code != "PAID") {
            val monthlyFines = monthlyPendingFinesService.getMonthlyPendingFines(
                entity.partner.id, 
                entity.billingPeriodStart.monthValue, 
                entity.billingPeriodStart.year
            )
            pendingFines = monthlyFines.jobAbsences + monthlyFines.meetingAbsences
            totalFinesAmount = monthlyFines.totalFines
        }
        
        return WaterBillOutputDto(
            id = entity.id,
            billNumber = entity.billNumber,
            partnerId = entity.partner.id,
            partnerName = entity.partner.fullName,
            partnerNumber = entity.partner.partnerNumber,
            readingId = entity.reading?.id,
            billingPeriodStart = entity.billingPeriodStart,
            billingPeriodEnd = entity.billingPeriodEnd,
            consumptionM3 = entity.consumptionM3,
            ratePerM3 = entity.ratePerM3,
            baseAmount = entity.baseAmount,
            totalAmount = entity.totalAmount,
            paidAmount = totalPaidAmount,
            remainingBalance = calculatedRemainingBalance,
            statusCode = entity.status.code,
            statusName = entity.status.name,
            dueDate = entity.dueDate,
            paidDate = entity.paidDate,
            isOverdue = isOverdue,
            concepts = concepts,
            pendingFines = pendingFines,
            totalFinesAmount = totalFinesAmount,
            totalPayableAmount = entity.totalAmount.add(totalFinesAmount),
            totalFinesPaid = totalFinesPaid,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun toWaterBillSummaryDto(entity: WaterBillEntity): WaterBillSummaryDto {
        val isOverdue = entity.dueDate.isBefore(LocalDate.now()) && entity.status.code != "PAID"
        // Formato: "Enero 2025" (solo mes y año)
        val monthName = getMonthName(entity.billingPeriodStart.monthValue)
        val capitalizedMonth = monthName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val billingPeriod = "$capitalizedMonth ${entity.billingPeriodStart.year}"

        return WaterBillSummaryDto(
            id = entity.id,
            billNumber = entity.billNumber,
            partnerName = entity.partner.fullName,
            billingPeriod = billingPeriod,
            totalAmount = entity.totalAmount,
            remainingBalance = entity.remainingBalance,
            status = entity.status.name,
            dueDate = entity.dueDate,
            isOverdue = isOverdue
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
}
