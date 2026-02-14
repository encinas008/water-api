package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.common.errorhandler.NotFoundEntityException
import com.dreamsbo.posapi.dto.BillingConfigOutputDto
import com.dreamsbo.posapi.dto.BillingConfigUpdateDto
import com.dreamsbo.posapi.persistence.entity.BillingConfigEntity
import com.dreamsbo.posapi.persistence.repository.BillingConfigRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class BillingConfigService(
    private val billingConfigRepository: BillingConfigRepository
) {

    /**
     * Obtiene todas las configuraciones activas
     */
    fun getAllConfigs(): List<BillingConfigOutputDto> {
        return billingConfigRepository.findAllByActive(true)
            .map { toOutputDto(it) }
    }

    /**
     * Obtiene una configuración específica por su clave
     */
    fun getConfigByKey(key: String): BillingConfigOutputDto {
        return billingConfigRepository.findByConfigKeyAndActive(key, true)
            .map { toOutputDto(it) }
            .orElseGet {
                // Si no existe, devolvemos un objeto virtual con valores por defecto para no romper la UI
                val defaultValue = when(key) {
                    "MULTA_CORTE" -> BigDecimal("50.0")
                    "MANTENIMIENTO_SUSPENDIDA" -> BigDecimal("5.0")
                    "TARIFA_BASICA" -> BigDecimal("15.0")
                    "MULTA_EXCESO_M3" -> BigDecimal("5.0")
                    "APORTE_DEPORTE" -> BigDecimal("2.0")
                    "APORTE_OTB" -> BigDecimal("3.0")
                    "MULTA_RETRASO_AULL" -> BigDecimal("5.0")
                    "MULTA_RETRASO_CLASICO" -> BigDecimal("5.0")
                    "MULTA_CONEXION_PASIVA" -> BigDecimal("5.0")
                    else -> BigDecimal.ZERO
                }
                BillingConfigOutputDto(
                    id = UUID.randomUUID(),
                    configKey = key,
                    configValue = defaultValue,
                    description = "Configuración por defecto (Autogenerada)"
                )
            }
    }

    /**
     * Obtiene el valor de una configuración con fallback a valor por defecto
     * Este método es usado por WaterBillingService para obtener valores dinámicos
     */
    fun getConfigValue(key: String, defaultValue: BigDecimal): BigDecimal {
        return billingConfigRepository.findByConfigKeyAndActive(key, true)
            .map { it.configValue }
            .orElse(defaultValue)
    }

    /**
     * Actualiza el valor de una configuración existente
     */
    fun updateConfig(key: String, input: BillingConfigUpdateDto): BillingConfigOutputDto {
        val config = billingConfigRepository.findByConfigKeyAndActive(key, true)
            .orElseGet {
                BillingConfigEntity(
                    configKey = key,
                    configValue = input.configValue,
                    description = "Configuración de $key"
                )
            }

        config.configValue = input.configValue
        config.updatedAt = java.time.OffsetDateTime.now()
        val savedConfig = billingConfigRepository.save(config)

        return toOutputDto(savedConfig)
    }

    private fun toOutputDto(entity: BillingConfigEntity): BillingConfigOutputDto {
        return BillingConfigOutputDto(
            id = entity.id,
            configKey = entity.configKey,
            configValue = entity.configValue,
            description = entity.description
        )
    }
}
