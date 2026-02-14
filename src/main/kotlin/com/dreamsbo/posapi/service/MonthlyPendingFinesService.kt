package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.MonthlyPendingFinesDto
import com.dreamsbo.posapi.dto.PendingFineDto
import com.dreamsbo.posapi.persistence.repository.JobAttendanceRepository
import com.dreamsbo.posapi.persistence.repository.MeetingAttendanceRepository
import com.dreamsbo.posapi.persistence.repository.PartnerRepository
import com.dreamsbo.posapi.persistence.repository.WaterPaymentDetailRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class MonthlyPendingFinesService(
    private val jobAttendanceRepository: JobAttendanceRepository,
    private val meetingAttendanceRepository: MeetingAttendanceRepository,
    private val partnerRepository: PartnerRepository,
    private val waterPaymentDetailRepository: WaterPaymentDetailRepository,
    private val billingConfigService: BillingConfigService
) {

    fun getMonthlyPendingFines(partnerId: UUID, month: Int, year: Int): MonthlyPendingFinesDto {
        partnerRepository.findById(partnerId)
            .orElseThrow { NotFoundEntityException("No se ha encontrado el socio. PartnerId = $partnerId") }

        // Calcular rango de fechas del mes
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        // Obtener IDs de multas ya pagadas para este socio
        val paidFineIds = waterPaymentDetailRepository.findPaidFineIdsByPartner(partnerId)

        // Obtener ausencias de trabajos (filtrar pagadas)
        val jobAbsences = jobAttendanceRepository.findAbsencesByPartnerAndDateRange(
            partnerId, startDate, endDate, true
        ).filter { it.id !in paidFineIds }
        .map { attendance ->
            val fine = attendance.job.fine ?: BigDecimal.ZERO
            PendingFineDto(
                id = attendance.id,
                type = "TRABAJO",
                name = attendance.job.name,
                date = attendance.attendanceDate,
                fine = fine
            )
        }

        // Obtener multas de reuniones (inasistencia o retraso)
        val meetingAbsences = meetingAttendanceRepository.findFinesByPartnerAndDateRange(
            partnerId, startDate, endDate, true
        ).filter { it.id !in paidFineIds }
        .map { attendance ->
            // Si es falta, se usa la multa de la reunión. Si es retraso, se usa lateFine.
            val fine = if (!attendance.present) {
                attendance.meeting.fine
            } else {
                attendance.lateFine
            }
            
            val typeExtra = if (attendance.present && attendance.lateFine > BigDecimal.ZERO) " (RETRASO)" else ""
            
            PendingFineDto(
                id = attendance.id,
                type = "REUNION",
                name = attendance.meeting.name + typeExtra,
                date = attendance.attendanceDate,
                fine = fine
            )
        }

        // --- NUEVA LÓGICA PARA CONEXIÓN PASIVA (INACTIVE) ---
        val passiveFines = mutableListOf<PendingFineDto>()
        val partner = partnerRepository.findById(partnerId).get()
        if (partner.connectionStatus?.code == "INACTIVE") {
            // Solo agregar si no ha sido pagada (en este caso es mensual, así que verificamos por periodo)
            // Nota: El filtrado contra la factura ya generada se hace en WaterBillingService.toWaterBillOutputDto
            // Aquí solo la reportamos como una multa "potencial" del periodo.
            
            val fineAmount = billingConfigService.getConfigValue("MULTA_CONEXION_PASIVA", BigDecimal("5.0"))
            
            if (fineAmount > BigDecimal.ZERO) {
                passiveFines.add(
                    PendingFineDto(
                        id = UUID.nameUUIDFromBytes("passive-${partnerId}-${year}-${month}".toByteArray()),
                        type = "OTRO",
                        name = "Multa por conexión pasiva",
                        date = startDate,
                        fine = fineAmount
                    )
                )
            }
        }

        // Calcular total de multas
        val totalFines = jobAbsences.sumOf { it.fine } + meetingAbsences.sumOf { it.fine } + passiveFines.sumOf { it.fine }

        return MonthlyPendingFinesDto(
            partnerId = partnerId,
            month = month,
            year = year,
            jobAbsences = jobAbsences,
            meetingAbsences = meetingAbsences + passiveFines,
            totalFines = totalFines
        )
    }

    fun getCurrentMonthPendingFines(partnerId: UUID): MonthlyPendingFinesDto {
        val now = LocalDate.now()
        return getMonthlyPendingFines(partnerId, now.monthValue, now.year)
    }

    fun getAllUnpaidFines(partnerId: UUID): List<PendingFineDto> {
        val paidFineIds = waterPaymentDetailRepository.findPaidFineIdsByPartner(partnerId)
        
        val jobAbsences = jobAttendanceRepository.findAbsencesByPartner(partnerId, true)
            .filter { it.id !in paidFineIds }
            .map { attendance ->
                PendingFineDto(
                    id = attendance.id,
                    type = "TRABAJO",
                    name = attendance.job.name,
                    date = attendance.attendanceDate,
                    fine = attendance.job.fine ?: BigDecimal.ZERO
                )
            }
            
        val meetingAbsences = meetingAttendanceRepository.findFinesByPartner(partnerId, true)
            .filter { it.id !in paidFineIds }
            .map { attendance ->
                val fine = if (!attendance.present) {
                    attendance.meeting.fine
                } else {
                    attendance.lateFine
                }
                
                val typeExtra = if (attendance.present && attendance.lateFine > BigDecimal.ZERO) " (RETRASO)" else ""
                
                PendingFineDto(
                    id = attendance.id,
                    type = "REUNION",
                    name = attendance.meeting.name + typeExtra,
                    date = attendance.attendanceDate,
                    fine = fine
                )
            }
            
        return jobAbsences + meetingAbsences
    }
}

