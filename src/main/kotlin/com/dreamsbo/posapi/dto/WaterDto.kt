package com.dreamsbo.posapi.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

// Water Meter Reading DTOs
data class WaterMeterReadingInputDto(
    val partnerId: UUID,
    val readingDate: LocalDate,
    val currentReading: BigDecimal,
    val observation: String = "",
    val imageId: UUID? = null,
    val userId: UUID? = null  // Opcional: usuario que registra la lectura
)

data class WaterMeterReadingOutputDto(
    val id: UUID,
    val partnerId: UUID,
    val partnerName: String,
    val readingDate: LocalDate,
    val previousReading: BigDecimal,
    val currentReading: BigDecimal,
    val consumption: BigDecimal,
    val readerUserName: String,
    val observation: String,
    val imageUrl: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class WaterMeterReadingUpdateDto(
    val readingDate: LocalDate?,
    val currentReading: BigDecimal?,
    val observation: String?
)

// Water Bill DTOs
data class WaterBillGenerationDto(
    val billingPeriodStart: LocalDate,
    val billingPeriodEnd: LocalDate,
    val ratePerM3: BigDecimal,
    val dueDate: LocalDate,
    val partnerIds: List<UUID>? = null // null means all active partners
)

data class WaterBillInputDto(
    val partnerId: UUID,
    val readingId: UUID? = null, // Opcional: lectura asociada
    val billingPeriodStart: LocalDate,
    val billingPeriodEnd: LocalDate,
    val consumptionM3: BigDecimal,
    val ratePerM3: BigDecimal,
    val dueDate: LocalDate
)

data class BillConceptItemDto(
    val id: UUID,
    val conceptName: String,
    val assignedDate: LocalDate,
    val amount: BigDecimal
)

data class WaterBillOutputDto(
    val id: UUID,
    val billNumber: String,
    val partnerId: UUID,
    val partnerName: String,
    val partnerNumber: Long?,
    val readingId: UUID?,
    val billingPeriodStart: LocalDate,
    val billingPeriodEnd: LocalDate,
    val consumptionM3: BigDecimal,
    val ratePerM3: BigDecimal,
    val baseAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val paidAmount: BigDecimal,
    val remainingBalance: BigDecimal,
    val statusCode: String,
    val statusName: String,
    val dueDate: LocalDate,
    val paidDate: LocalDate?,
    val isOverdue: Boolean,
    val concepts: List<BillConceptItemDto> = emptyList(),  // Conceptos de cobro desglosados
    val pendingFines: List<PendingFineDto> = emptyList(), // Multas pendientes (trabajos/reuniones)
    val totalFinesAmount: BigDecimal = BigDecimal.ZERO, // Suma de multas pendientes
    val totalPayableAmount: BigDecimal = BigDecimal.ZERO, // totalAmount + totalFinesAmount
    val totalFinesPaid: BigDecimal = BigDecimal.ZERO,  // Total de multas pagadas en esta factura
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?
)

data class WaterBillSummaryDto(
    val id: UUID,
    val billNumber: String,
    val partnerName: String,
    val billingPeriod: String,
    val totalAmount: BigDecimal,
    val remainingBalance: BigDecimal,
    val status: String,
    val dueDate: LocalDate,
    val isOverdue: Boolean
)

// Water Payment DTOs
data class WaterPaymentInputDto(
    val userId: UUID,
    val waterBillId: UUID?,
    val partnerId: UUID,
    val paymentDate: LocalDate,
    val amount: BigDecimal,
    val paymentTypeId: UUID,
    val cashBalanceId: UUID?,
    val observation: String = "",
    val includePendingFines: Boolean = false // Si incluir multas pendientes del mes
)

data class PaymentFineDetailDto(
    val id: UUID,
    val type: String, // "JOB" o "MEETING"
    val name: String,
    val date: LocalDate,
    val fineAmount: BigDecimal
)

data class PaymentDetailDto(
    val billAmount: BigDecimal,
    val finesAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val jobFines: List<PaymentFineDetailDto> = emptyList(),
    val meetingFines: List<PaymentFineDetailDto> = emptyList()
)

data class WaterPaymentOutputDto(
    val id: UUID,
    val waterBillId: UUID?,
    val billNumber: String?,
    val partnerId: UUID,
    val partnerName: String,
    val paymentDate: LocalDate,
    val amount: BigDecimal,
    val paymentTypeName: String,
    val receiptNumber: String,
    val cashierName: String,
    val observation: String,
    val correlativeNumber: Int? = null,
    val paymentDetail: PaymentDetailDto? = null, // Detalle del pago incluyendo multas
    val createdAt: OffsetDateTime
)

data class PaymentReceiptDto(
    val receiptNumber: String,
    val paymentDate: LocalDate,
    val partnerName: String,
    val partnerIdentificationNumber: String?,
    val billNumber: String,
    val billingPeriod: String,
    val amount: BigDecimal,
    val paymentTypeName: String,
    val cashierName: String,
    val previousBalance: BigDecimal,
    val newBalance: BigDecimal,
    val correlativeNumber: Int? = null
)

// Pending Fines DTOs
data class PendingFineDto(
    val id: UUID,
    val type: String, // "JOB" o "MEETING"
    val name: String,
    val date: LocalDate,
    val fine: BigDecimal
)

data class MonthlyPendingFinesDto(
    val partnerId: UUID,
    val month: Int,
    val year: Int,
    val jobAbsences: List<PendingFineDto>,
    val meetingAbsences: List<PendingFineDto>,
    val totalFines: BigDecimal
)

// Report DTOs
data class DebtReportDto(
    val partnerId: UUID,
    val partnerName: String,
    val partnerIdentificationNumber: String?,
    val totalDebt: BigDecimal,
    val pendingBillsCount: Int,
    val overdueBillsCount: Int,
    val oldestDebtDate: LocalDate?,
    val connectionStatus: String,
    val lastPaymentDate: LocalDate?,
    val contactPhone: String?
)

data class ConsumptionReportDto(
    val partnerId: UUID,
    val partnerName: String,
    val month: String,
    val year: Int,
    val consumption: BigDecimal,
    val billedAmount: BigDecimal,
    val paidAmount: BigDecimal,
    val status: String
)

data class CollectionReportDto(
    val period: String,
    val totalBilled: BigDecimal,
    val totalCollected: BigDecimal,
    val totalPending: BigDecimal,
    val billsGenerated: Int,
    val billsPaid: Int,
    val billsPending: Int,
    val billsOverdue: Int,
    val collectionRate: Double
)

// DTO para recibo de pago completo (basado en la imagen)
data class PaymentReceiptFullDto(
    val receiptNumber: String,
    val receiptType: String,  // "NOTA DE PAGO" o "COPIA ARCHIVO"
    val partnerNumber: String,  // Número del socio (ej: "31")
    val partnerName: String,
    val partnerIdentificationNumber: String?,  // RECLAMOS
    val paymentDate: String,  // Fecha y hora de pago formateada
    val currentReading: BigDecimal,  // LECTURA ACT.
    val previousReading: BigDecimal,  // LECTURA ANT.
    val consumptionM3: BigDecimal,  // CONSUMO M3
    val meterNumber: String?,  // NRO. MEDIDOR
    val paymentMonth: String,  // MES DE PAGO (ej: "JUNIO")
    val paymentMonthCode: String?,  // CÓDIGO
    val paymentMonthDate: String,  // Date formateada (ej: "31-junio-2025")
    val concepts: List<BillConceptItemDto>,  // Conceptos desglosados
    val totalAmount: BigDecimal,  // IMPORTE TOTAL
    val totalAmountInWords: String,  // Total en palabras (ej: "Son Veinte Bolivianos.")
    val correlativeNumber: Int? = null,
    val communityName: String = "COMUNIDAD GUADALUPE"  // Nombre de la comunidad
)

data class PendingReadingsReportDto(
    val partnerId: UUID,
    val partnerName: String,
    val waterMeterNumber: String?,
    val lastReadingDate: LocalDate?,
    val daysSinceLastReading: Int?,
    val address: String?,
    val contactPhone: String?
)

// DTO para detalle completo de factura con pagos
data class WaterBillDetailDto(
    val bill: WaterBillOutputDto,
    val payments: List<WaterPaymentOutputDto>
)
