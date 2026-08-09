package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.persistence.repository.JobRepository
import com.dreamsbo.posapi.persistence.repository.MeetingRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Service
class MaintenanceScheduledTaskService(
    private val jobRepository: JobRepository,
    private val meetingRepository: MeetingRepository,
    private val billingConfigService: BillingConfigService
) {
    private val logger = LoggerFactory.getLogger(MaintenanceScheduledTaskService::class.java)

    /**
     * Cron Job: Se ejecuta cada Lunes, Miércoles y Viernes a las 00:00.
     * Bloquea trabajos y reuniones que ya tienen más días de antigüedad que la configuración.
     */
    @Scheduled(cron = "0 0 0 * * MON,WED,FRI")
    @Transactional
    fun lockOldRecords() {
        // Por defecto no bloqueamos nada (usamos 9999 días)
        val diasBloqueo = billingConfigService.getConfigValue("DIAS_BLOQUEO_ASISTENCIA", BigDecimal("9999")).toLong()
        val today = LocalDate.now()
        val limitDate = today.minusDays(diasBloqueo)
        
        logger.info("🕒 Iniciando proceso de bloqueo automático de registros (Límite: $limitDate, Días: $diasBloqueo)")

        // Bloquear trabajos antiguos
        val jobsToLock = jobRepository.findAllByActive(true).filter { 
            !it.locked && it.startDate.isBefore(limitDate) 
        }
        jobsToLock.forEach { 
            it.locked = true
            it.updatedAt = OffsetDateTime.now()
        }
        if (jobsToLock.isNotEmpty()) {
            jobRepository.saveAll(jobsToLock)
            logger.info("✅ Se bloquearon ${jobsToLock.size} trabajos antiguos.")
        }

        // Bloquear reuniones antiguas
        val meetingsToLock = meetingRepository.findAllByActive(true).filter { 
            !it.locked && it.meetingDate.isBefore(limitDate) 
        }
        meetingsToLock.forEach { 
            it.locked = true
            it.updatedAt = OffsetDateTime.now()
        }
        if (meetingsToLock.isNotEmpty()) {
            meetingRepository.saveAll(meetingsToLock)
            logger.info("✅ Se bloquearon ${meetingsToLock.size} reuniones antiguas.")
        }
        
        logger.info("🏁 Fin del proceso de bloqueo automático.")
    }
}
