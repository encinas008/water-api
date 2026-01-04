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
        val config = billingConfigRepository.findByConfigKeyAndActive(key, true)
            .orElseThrow { NotFoundEntityException("Configuración no encontrada: $key") }
        return toOutputDto(config)
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
            .orElseThrow { NotFoundEntityException("Configuración no encontrada: $key") }

        config.configValue = input.configValue
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
