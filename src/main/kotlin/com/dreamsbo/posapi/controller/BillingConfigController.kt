package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.BillingConfigOutputDto
import com.dreamsbo.posapi.dto.BillingConfigUpdateDto
import com.dreamsbo.posapi.service.BillingConfigService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/billing-config")
class BillingConfigController(
    private val billingConfigService: BillingConfigService
) {

    /**
     * Obtener todas las configuraciones de facturación
     * GET /billing-config
     */
    @GetMapping
    fun getAllConfigs(): ResponseEntity<List<BillingConfigOutputDto>> {
        val configs = billingConfigService.getAllConfigs()
        return ResponseEntity.ok(configs)
    }

    /**
     * Obtener una configuración específica por clave
     * GET /billing-config/{key}
     */
    @GetMapping("/{key}")
    fun getConfigByKey(@PathVariable key: String): ResponseEntity<BillingConfigOutputDto> {
        val config = billingConfigService.getConfigByKey(key)
        return ResponseEntity.ok(config)
    }

    /**
     * Actualizar una configuración (solo administradores)
     * PUT /billing-config/{key}
     */
    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateConfig(
        @PathVariable key: String,
        @RequestBody input: BillingConfigUpdateDto
    ): ResponseEntity<BillingConfigOutputDto> {
        val updatedConfig = billingConfigService.updateConfig(key, input)
        return ResponseEntity.ok(updatedConfig)
    }
}
