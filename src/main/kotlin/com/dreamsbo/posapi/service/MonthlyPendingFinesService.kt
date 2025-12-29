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
    private val waterPaymentDetailRepository: WaterPaymentDetailRepository
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

        // Obtener ausencias de reuniones (filtrar pagadas)
        val meetingAbsences = meetingAttendanceRepository.findAbsencesByPartnerAndDateRange(
            partnerId, startDate, endDate, true
        ).filter { it.id !in paidFineIds }
        .map { attendance ->
            val fine = attendance.meeting.fine ?: BigDecimal.ZERO
            PendingFineDto(
                id = attendance.id,
                type = "REUNION",
                name = attendance.meeting.name,
                date = attendance.attendanceDate,
                fine = fine
            )
        }

        // Calcular total de multas
        val totalFines = jobAbsences.sumOf { it.fine } + meetingAbsences.sumOf { it.fine }

        return MonthlyPendingFinesDto(
            partnerId = partnerId,
            month = month,
            year = year,
            jobAbsences = jobAbsences,
            meetingAbsences = meetingAbsences,
            totalFines = totalFines
        )
    }

    fun getCurrentMonthPendingFines(partnerId: UUID): MonthlyPendingFinesDto {
        val now = LocalDate.now()
        return getMonthlyPendingFines(partnerId, now.monthValue, now.year)
    }
}

