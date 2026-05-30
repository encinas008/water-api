package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.BadRequestException
import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.AddBillConceptDto
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    private val cashFlowService: CashFlowService,
    private val cashFlowTypeRepository: CashFlowTypeRepository,
    private val billingConfigService: BillingConfigService,
    private val connectionStatusTypeRepository: ConnectionStatusTypeRepository,
    @Value("\${WATER_BILLING_BASIC_LIMIT:15.0}")
    private val basicConsumptionLimit: Double = 15.0
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
            // Prevent duplicate bills for the same month and partner
            val billExists = waterBillRepository.existsByPartnerIdAndBillingPeriodStartAndActive(
                partner.id,
                input.billingPeriodStart,
                true
            )
            
            if (!billExists) {
                // Obtener la lectura específica de ese mes
                val readingForMonthList = waterMeterReadingRepository.findByPartnerIdAndYearAndMonth(
                    partner.id,
                    input.billingPeriodStart.year,
                    input.billingPeriodStart.monthValue,
                    true
                )
                val readingForMonth = readingForMonthList.firstOrNull()
                
                // Si no hay lectura para este mes, saltar y no generar factura
                if (readingForMonth == null) {
                    return@forEach
                }

                val consumption = readingForMonth.consumption
                
                // Nueva lógica: Los primeros m3 (configurables por ENV) están incluidos en la Tarifa Básica
                val threshold = BigDecimal.valueOf(basicConsumptionLimit)
                val baseAmount = if (consumption <= threshold) {
                    BigDecimal.ZERO
                } else {
                    val multaExcesoM3 = billingConfigService.getConfigValue("MULTA_EXCESO_M3", BigDecimal("5.0"))
                    val excessM3 = consumption.subtract(threshold).setScale(0, RoundingMode.DOWN)
                    excessM3.multiply(multaExcesoM3)
                }

                val billNumber = generateBillNumber(partner.id)

                val bill = WaterBillEntity(
                    billNumber = billNumber,
                    partner = partner,
                    reading = readingForMonth,
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
                
                // Si el total es 0, marcar como PAGADA automáticamente
                if (totalFromConcepts <= BigDecimal.ZERO) {
                    val paidStatus = billStatusTypeRepository.findByCodeAndActive("PAID", true).orElse(null)
                    if (paidStatus != null) {
                        savedBill.status = paidStatus
                        savedBill.paidDate = LocalDate.now()
                    }
                }
                
                val finalBill = waterBillRepository.save(savedBill)

                generatedBills.add(finalBill)

                // Update partner's last billing date and debt
                partner.lastBillingDate = LocalDate.now()
                val previousDebt = partner.currentDebt
                partner.currentDebt = partner.currentDebt.add(finalBill.totalAmount)
                
                // --- NUEVA LÓGICA DE CONTROL DE CORTE Y MULTAS ---
                checkAndApplyAutoCutoff(partner, finalBill)
                
                partnerRepository.save(partner)
            }
        }

        return generatedBills.map { toWaterBillOutputDto(it) }
    }


    fun previewMonthlyBills(year: Int, month: Int): com.dreamsbo.posapi.dto.WaterBillGenerationPreviewDto {
        val activePartners = partnerRepository.findAllByActive(true, Sort.unsorted())
        
        val toGenerate = mutableListOf<com.dreamsbo.posapi.dto.WaterBillPreviewItemDto>()
        val missingReadings = mutableListOf<com.dreamsbo.posapi.dto.WaterBillPreviewItemDto>()
        
        activePartners.forEach { partner ->
            // Check if bill already exists
            val periodStart = LocalDate.of(year, month, 1)
            val billExists = waterBillRepository.existsByPartnerIdAndBillingPeriodStartAndActive(
                partner.id,
                periodStart,
                true
            )
            
            if (!billExists) {
                val readingForMonth = waterMeterReadingRepository.findByPartnerIdAndYearAndMonth(
                    partner.id,
                    year,
                    month,
                    true
                ).firstOrNull()
                
                val item = com.dreamsbo.posapi.dto.WaterBillPreviewItemDto(
                    partnerId = partner.id,
                    partnerName = partner.fullName,
                    partnerNumber = partner.partnerNumber.toString(),
                    hasReading = readingForMonth != null,
                    readingValue = readingForMonth?.consumption
                )
                
                if (readingForMonth != null) {
                    toGenerate.add(item)
                } else {
                    missingReadings.add(item)
                }
            }
        }
        
        // Sort for better presentation
        toGenerate.sortBy { it.partnerName }
        missingReadings.sortBy { it.partnerName }
        
        return com.dreamsbo.posapi.dto.WaterBillGenerationPreviewDto(
            month = month,
            year = year,
            toGenerateCount = toGenerate.size,
            missingReadingsCount = missingReadings.size,
            toGenerate = toGenerate,
            missingReadings = missingReadings
        )
    }

    fun calculateBillAmount(consumption: BigDecimal, ratePerM3: BigDecimal): BigDecimal {
        return consumption * ratePerM3
    }

    /**
     * Verifica si el socio debe ser cortado por mora (4 meses) 
     * o si debe recibir una multa recurrente por estar en estado cortado (cada 3 meses).
     */
    private fun checkAndApplyAutoCutoff(partner: com.dreamsbo.posapi.persistence.entity.PartnerEntity, bill: WaterBillEntity) {
        val unpaidBillsCount = waterBillRepository.countUnpaidBillsByPartnerId(partner.id)
        val currentStatus = partner.connectionStatus?.code ?: "ACTIVE"
        
        // El monto de la multa es 50 Bs según requerimiento
        val multaCorteMonto = billingConfigService.getConfigValue("MULTA_CORTE", BigDecimal("50.0"))
        
        if (currentStatus == "ACTIVE" && unpaidBillsCount >= 3) {
            println("🚨 SOCIO CON MORA (3 MESES). Cambiando estado a CUT_OFF y aplicando multa de $multaCorteMonto Bs")
            
            // Cambiar estado a CORTADO
            val cutOffStatus = connectionStatusTypeRepository.findByCodeAndActive("CUT_OFF", true)
                .orElse(null)
            if (cutOffStatus != null) {
                partner.connectionStatus = cutOffStatus
                partner.statusChangedAt = OffsetDateTime.now()
                
                // Aplicar multa de 50 Bs por el corte inicial
                val disconnectionFine = BillConceptItemEntity(
                    waterBill = bill,
                    conceptName = "Multa por corte de servicio (Mora acumulada de 3 meses)",
                    assignedDate = LocalDate.now(),
                    amount = multaCorteMonto
                )
                billConceptItemRepository.save(disconnectionFine)
                
                // Actualizar totales de la factura
                bill.totalAmount = bill.totalAmount.add(multaCorteMonto)
                bill.remainingBalance = bill.remainingBalance.add(multaCorteMonto)
                waterBillRepository.save(bill)
                
                // Actualizar deuda del socio
                partner.currentDebt = partner.currentDebt.add(multaCorteMonto)
            }
        }
        // Nota: El recargo recurrente por permanecer en estado CORTADO se maneja ahora en createDefaultBillConcepts
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

    fun getBillStats(search: String? = null, statusCode: String? = null): WaterBillStatsDto {
        val stats = waterBillRepository.getBillStats(LocalDate.now(), true, search, statusCode).firstOrNull()

        return if (stats != null) {
            WaterBillStatsDto(
                totalBills = stats[0] as? Long ?: 0L,
                pendingBillsCount = stats[1] as? Long ?: 0L,
                overdueBillsCount = stats[2] as? Long ?: 0L,
                paidBillsCount = stats[3] as? Long ?: 0L,
                totalPendingAmount = stats[4] as? BigDecimal ?: BigDecimal.ZERO,
                totalOverdueAmount = stats[5] as? BigDecimal ?: BigDecimal.ZERO,
                totalPaidAmount = stats[6] as? BigDecimal ?: BigDecimal.ZERO
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
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "billingPeriodStart"))
        
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
        
        // Nueva lógica: Los primeros m3 (configurables por ENV) están incluidos en la Tarifa Básica
        val threshold = BigDecimal.valueOf(basicConsumptionLimit)
        val excessAmount = if (consumption <= threshold) {
            BigDecimal.ZERO
        } else {
            val multaExcesoM3 = billingConfigService.getConfigValue("MULTA_EXCESO_M3", BigDecimal("5.0"))
            val excessM3 = consumption.subtract(threshold).setScale(0, RoundingMode.DOWN)
            excessM3.multiply(multaExcesoM3)
        }
        val baseAmount = excessAmount

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
        
        // --- NUEVA LÓGICA DE CONTROL DE CORTE Y MULTAS ---
        checkAndApplyAutoCutoff(partner, finalBill)
        
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
        
        // Nueva lógica: Los primeros m3 (configurables por ENV) están incluidos en la Tarifa Básica
        val threshold = BigDecimal.valueOf(basicConsumptionLimit)
        val excessAmount = if (input.consumptionM3 <= threshold) {
            BigDecimal.ZERO
        } else {
            val multaExcesoM3 = billingConfigService.getConfigValue("MULTA_EXCESO_M3", BigDecimal("5.0"))
            val excessM3 = input.consumptionM3.subtract(threshold).setScale(0, RoundingMode.DOWN)
            excessM3.multiply(multaExcesoM3)
        }
        val baseAmount = excessAmount
        
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
        
        // La nueva deuda es: Deuda Anterior + Total Factura
        // (El total de la factura ya incluye las multas del mes si fueron agregadas como conceptos por createDefaultBillConcepts)
        partner.currentDebt = partner.currentDebt.add(finalBill.totalAmount)
        
        // --- NUEVA LÓGICA DE CONTROL DE CORTE Y MULTAS ---
        checkAndApplyAutoCutoff(partner, finalBill)
        
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
        val currentStatus = bill.partner.connectionStatus?.code?.uppercase()?.trim() ?: "ACTIVE"
        val isCutOff = currentStatus == "CUT_OFF"
        val isInactive = currentStatus == "INACTIVE"
        val isSuspended = currentStatus == "SUSPENDED"

        val allConcepts = mutableListOf<BillConceptItemEntity>()

        // --- LÓGICA PARA SOCIOS SUSPENDIDOS ---
        // Si el socio está suspendido, no paga tarifa básica ni otros aportes. 
        // Solo paga el mantenimiento mensual configurado.
        if (isSuspended) {
            val maintenanceFeeMonto = billingConfigService.getConfigValue("MANTENIMIENTO_SUSPENDIDA", BigDecimal("5.0"))
            val maintenanceFee = BillConceptItemEntity(
                waterBill = bill,
                conceptName = "Pago por mantenimiento (Socio Suspendido)",
                assignedDate = assignedDate,
                amount = maintenanceFeeMonto
            )
            allConcepts.add(maintenanceFee)
        } else {
            // Obtener valores configurables (para ACTIVOS, CUT_OFF, INACTIVE)
            val tarifaBasicaValue = billingConfigService.getConfigValue("TARIFA_BASICA", BigDecimal("15.0"))
            val multaExcesoM3 = billingConfigService.getConfigValue("MULTA_EXCESO_M3", BigDecimal("5.0"))
            val aporteDeporte = billingConfigService.getConfigValue("APORTE_DEPORTE", BigDecimal("2.0"))
            val aporteOTB = billingConfigService.getConfigValue("APORTE_OTB", BigDecimal("3.0"))
            
            // Crear concepto para el consumo excedente si aplica (válido incluso para CUT_OFF/INACTIVE si hubo consumo)
            val threshold = BigDecimal.valueOf(basicConsumptionLimit)
            val excessM3 = if (bill.consumptionM3 > threshold) bill.consumptionM3.subtract(threshold).setScale(0, RoundingMode.DOWN) else BigDecimal.ZERO
            
            val excessConcept = if (excessM3 > BigDecimal.ZERO) {
                BillConceptItemEntity(
                    waterBill = bill,
                    conceptName = "Multa por exceso de consumo de agua ($excessM3 m³ × $multaExcesoM3 Bs/m³)",
                    assignedDate = assignedDate,
                    amount = bill.baseAmount // Ya calculado como excedente redondeado en generateBill
                )
            } else null
            
            // Conceptos adicionales fijos (Se omiten solo para socios INACTIVE)
            if (!isInactive) {
                allConcepts.add(
                    BillConceptItemEntity(
                        waterBill = bill,
                        conceptName = "Aporte al deporte",
                        assignedDate = assignedDate,
                        amount = aporteDeporte
                    )
                )
                allConcepts.add(
                    BillConceptItemEntity(
                        waterBill = bill,
                        conceptName = "Tarifa Básica",
                        assignedDate = assignedDate,
                        amount = tarifaBasicaValue
                    )
                )
                allConcepts.add(
                    BillConceptItemEntity(
                        waterBill = bill,
                        conceptName = "Aporte a la OTB",
                        assignedDate = assignedDate,
                        amount = aporteOTB
                    )
                )
                
                // Recargo recurrente cada 3 meses para socios CORTADOS
                if (isCutOff) {
                    val statusChangedAt = bill.partner.statusChangedAt ?: bill.partner.createdAt
                    val currentBillMonth = assignedDate.withDayOfMonth(1)
                    val statusMonth = statusChangedAt.toLocalDate().withDayOfMonth(1)
                    val monthsInStatus = ChronoUnit.MONTHS.between(statusMonth, currentBillMonth)
                    
                    val mesesIntervalo = billingConfigService.getConfigValue("MESES_PARA_CARGO_CORTE", BigDecimal("3")).toLong()
                    val montoRecurrente = billingConfigService.getConfigValue("CARGO_POR_CORTE_RECURRENTE", BigDecimal("50.0"))
                    
                    if (monthsInStatus > 0 && monthsInStatus % mesesIntervalo == 0L) {
                        allConcepts.add(
                            BillConceptItemEntity(
                                waterBill = bill,
                                conceptName = "Recargo recurrente por estado cortado ($monthsInStatus meses)",
                                assignedDate = assignedDate,
                                amount = montoRecurrente
                            )
                        )
                    }
                }
            }
            
            excessConcept?.let { allConcepts.add(it) }
        }

        // --- LÓGICA: Buscar multas de reuniones, trabajos y otros (como conexión pasiva) ---
        // IMPORTANTE: Las multas se agregan a TODOS los socios, incluyendo SUSPENDIDOS
        val monthlyFines = monthlyPendingFinesService.getMonthlyPendingFines(
            bill.partner.id, 
            assignedDate.monthValue, 
            assignedDate.year
        )
        
        val jobFines = monthlyFines.jobAbsences.map { fine ->
            val fineDate = formatDateLiteral(fine.date)
            BillConceptItemEntity(
                waterBill = bill,
                conceptName = "Multa Trabajo: ${fine.name} ($fineDate)",
                assignedDate = assignedDate,
                amount = fine.fine
            )
        }
        
        val meetingAndOtherFines = monthlyFines.meetingAbsences.map { fine ->
            val fineDate = formatDateLiteral(fine.date)
            val prefix = when(fine.type) {
                "REUNION" -> "Multa Reunión: "
                "TRABAJO" -> "Multa Trabajo: "
                else -> "" // Para OTRO (Conexión Pasiva) no ponemos prefijo ya que fine.name ya es descriptivo
            }
            BillConceptItemEntity(
                waterBill = bill,
                conceptName = "$prefix${fine.name} ($fineDate)",
                assignedDate = assignedDate,
                amount = fine.fine
            )
        }
        
        // Agregar multas a los conceptos
        allConcepts.addAll(jobFines)
        allConcepts.addAll(meetingAndOtherFines)
        
        billConceptItemRepository.saveAll(allConcepts)
        
        // Retornar total
        return allConcepts.sumOf { it.amount }
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
            val allFines = monthlyFines.jobAbsences + monthlyFines.meetingAbsences
            
            val conceptNames = concepts.map { it.conceptName.lowercase().trim() }
        pendingFines = allFines.filter { fine ->
            val fineNameLower = fine.name.lowercase().trim()
            conceptNames.none { it.contains(fineNameLower) }
        }
        totalFinesAmount = pendingFines.sumOf { it.fine }
    }
    
    // El totalPayableAmount es la suma del saldo de la factura y las multas pendientes
    val totalPayableAmount = calculatedRemainingBalance.add(totalFinesAmount)
        
        // Reconciliation: If the database status is inconsistent with the payments found,
        // we use a computed status for the DTO. This is common after migrations.
        val effectiveStatusCode = when {
            entity.status.code == "CANCELLED" -> "CANCELLED"
            entity.status.code == "PAID" -> "PAID"
            // Only force PAID if there's actual payment history or if it was already marked as such
            // This avoids showing 0-amount PENDING bills as PAID if they haven't been processed yet
            totalPaidAmount > BigDecimal.ZERO && calculatedRemainingBalance <= BigDecimal.ZERO -> "PAID"
            totalPaidAmount > BigDecimal.ZERO -> "PARTIAL_PAID"
            else -> entity.status.code
        }

        val effectiveStatusName = when (effectiveStatusCode) {
            "PAID" -> "PAGADA"
            "PARTIAL_PAID" -> "PAGO PARCIAL"
            "PENDING" -> "PENDIENTE"
            "CANCELLED" -> "CANCELADA"
            "OVERDUE" -> "VENCIDA"
            else -> entity.status.name
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
            statusCode = effectiveStatusCode,
            statusName = effectiveStatusName,
            partnerStatusCode = entity.partner.connectionStatus?.code,
            partnerStatusName = entity.partner.connectionStatus?.name,
            dueDate = entity.dueDate,
            paidDate = entity.paidDate ?: if (effectiveStatusCode == "PAID") LocalDate.now() else null,
            isOverdue = isOverdue,
            concepts = concepts,
            pendingFines = pendingFines,
            totalFinesAmount = totalFinesAmount,
            totalPayableAmount = totalPayableAmount,
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

    @Transactional
    fun addConceptToBill(billId: UUID, input: AddBillConceptDto): WaterBillOutputDto {
        val bill = waterBillRepository.findById(billId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la factura. BillId = $billId") }

        if (bill.status.code != "PENDING") {
            throw BadRequestException("Solo se pueden agregar conceptos a facturas en estado PENDIENTE")
        }

        // 1. Crear el nuevo concepto
        val newConcept = BillConceptItemEntity(
            waterBill = bill,
            conceptName = input.conceptName,
            assignedDate = input.assignedDate,
            amount = input.amount
        )
        billConceptItemRepository.save(newConcept)

        // 2. Actualizar totales de la factura
        bill.totalAmount = bill.totalAmount.add(input.amount)
        bill.remainingBalance = bill.remainingBalance.add(input.amount)
        bill.updatedAt = OffsetDateTime.now()

        val savedBill = waterBillRepository.save(bill)

        // 3. Actualizar deuda del socio
        val partner = bill.partner
        partner.currentDebt = partner.currentDebt.add(input.amount)
        partnerRepository.save(partner)

        return toWaterBillOutputDto(savedBill)
    }

    @Transactional
    fun removeConceptFromBill(billId: UUID, conceptId: UUID): WaterBillOutputDto {
        val bill = waterBillRepository.findById(billId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la factura. BillId = $billId") }

        if (bill.status.code != "PENDING") {
            throw BadRequestException("Solo se pueden eliminar conceptos de facturas en estado PENDIENTE")
        }

        val concept = billConceptItemRepository.findById(conceptId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el concepto. ConceptId = $conceptId") }

        if (concept.waterBill.id != billId) {
            throw BadRequestException("El concepto no pertenece a esta factura")
        }

        // 1. Desactivar el concepto
        concept.active = false
        billConceptItemRepository.save(concept)

        // 2. Actualizar totales de la factura
        bill.totalAmount = bill.totalAmount.subtract(concept.amount)
        bill.remainingBalance = bill.remainingBalance.subtract(concept.amount)
        bill.updatedAt = OffsetDateTime.now()
        val savedBill = waterBillRepository.save(bill)

        // 3. Actualizar deuda del socio
        val partner = bill.partner
        partner.currentDebt = partner.currentDebt.subtract(concept.amount)
        partnerRepository.save(partner)

        return toWaterBillOutputDto(savedBill)
    }

    // ──── Métodos auxiliares para uso desde WaterMeterReadingService ────
    fun findBillStatus(code: String) = billStatusTypeRepository.findByCodeAndActive(code, true)
        .orElseThrow { NotFoundEntityException("Estado $code no encontrado") }

    fun findPaymentsByBill(billId: UUID) = waterPaymentRepository.findByWaterBillIdAndActive(billId, true)

    fun savePayment(payment: com.dreamsbo.posapi.persistence.entity.WaterPaymentEntity) {
        waterPaymentRepository.save(payment)
    }

    fun findPaymentDetailsByPayment(paymentId: UUID) = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(paymentId, true)

    fun savePaymentDetails(details: List<com.dreamsbo.posapi.persistence.entity.WaterPaymentDetailEntity>) {
        waterPaymentDetailRepository.saveAll(details)
    }
    // ──────────────────────────────────────────────────────────────────

    @Transactional
    fun cancelBill(id: UUID, userId: UUID): WaterBillOutputDto {
        val bill = waterBillRepository.findById(id)
            .orElseThrow { NotFoundEntityException("No se ha encontrado la factura. BillId = $id") }

        if (bill.status.code == "CANCELLED") {
            throw BadRequestException("La factura ya se encuentra anulada")
        }

        val cancelledStatus = billStatusTypeRepository.findByCodeAndActive("CANCELLED", true)
            .orElseThrow { NotFoundEntityException("Estado CANCELLED no encontrado") }

        val pendingStatus = billStatusTypeRepository.findByCodeAndActive("PENDING", true)
            .orElseThrow { NotFoundEntityException("Estado PENDING no encontrado") }

        // 1. Manejar devoluciones si hay pagos realizados (sin generar egreso de caja)
        val payments = waterPaymentRepository.findByWaterBillIdAndActive(bill.id, true)
        var totalReversedAmount = BigDecimal.ZERO

        if (payments.isNotEmpty()) {
            payments.forEach { payment ->
                totalReversedAmount = totalReversedAmount.add(payment.amount)
                
                // Desactivar el pago sin registrar movimiento de caja
                payment.active = false
                waterPaymentRepository.save(payment)
                
                // Desactivar detalles del pago
                val details = waterPaymentDetailRepository.findByWaterPaymentIdAndActive(payment.id, true)
                details.forEach { it.active = false }
                waterPaymentDetailRepository.saveAll(details)
            }
        }

        // 2. Clonar la factura como PENDING con el mismo detalle
        val newBillNumber = generateBillNumber(bill.partner.id)
        val newBill = WaterBillEntity(
            billNumber = newBillNumber,
            partner = bill.partner,
            reading = bill.reading, // Mantener la lectura
            billingPeriodStart = bill.billingPeriodStart,
            billingPeriodEnd = bill.billingPeriodEnd,
            consumptionM3 = bill.consumptionM3,
            ratePerM3 = bill.ratePerM3,
            baseAmount = bill.baseAmount,
            totalAmount = bill.totalAmount,
            remainingBalance = bill.totalAmount, // Inicia con el total pendiente
            status = pendingStatus,
            dueDate = bill.dueDate
        )
        val savedNewBill = waterBillRepository.save(newBill)

        // 3. Clonar los conceptos a la nueva factura
        val concepts = billConceptItemRepository.findByWaterBillIdAndActive(bill.id, true)
        val newConcepts = concepts.map { oldConcept ->
            BillConceptItemEntity(
                waterBill = savedNewBill,
                conceptName = oldConcept.conceptName,
                assignedDate = oldConcept.assignedDate,
                amount = oldConcept.amount
            )
        }
        billConceptItemRepository.saveAll(newConcepts)

        // 4. Actualizar estado de la factura original a CANCELLED
        bill.status = cancelledStatus
        bill.updatedAt = OffsetDateTime.now()
        bill.remainingBalance = bill.totalAmount // Restaurar saldo original para consistencia conceptual
        
        // Desactivar los conceptos de la factura anulada
        concepts.forEach { it.active = false }
        billConceptItemRepository.saveAll(concepts)

        val savedOldBill = waterBillRepository.save(bill)

        // 5. Ajustar la deuda del socio
        val partner = bill.partner
        
        // La deuda bajaba por lo que restaba de la factura antigua
        val currentBillPending = bill.totalAmount.subtract(totalReversedAmount)
        partner.currentDebt = partner.currentDebt.subtract(currentBillPending)
        
        // Ahora sube por la nueva factura clonada PENDING
        partner.currentDebt = partner.currentDebt.add(savedNewBill.totalAmount)
        
        partnerRepository.save(partner)

        // Retornar la antigua factura cancelada para actualizar la vista
        return toWaterBillOutputDto(savedOldBill)
    }

    private fun formatDateLiteral(date: LocalDate): String {
        val day = String.format("%02d", date.dayOfMonth)
        val monthName = getMonthName(date.monthValue).replaceFirstChar { it.uppercase() }
        val year = date.year
        return "$day/$monthName/$year"
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
