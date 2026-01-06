-- ============================================
-- Script: add-new-billing-configs.sql
-- Descripción: Agrega nuevas claves de configuración para multas por corte y mantenimiento
-- Autor: Antigravity
-- Fecha: 2026-01-05
-- ============================================

-- Insertar nuevas configuraciones para corte y mantenimiento
INSERT INTO pos.billing_config (config_key, config_value, description) VALUES
    ('MULTA_CORTE', 50.00, 'Monto aplicado al cortar el servicio por mora acumulada (Bs)'),
    ('MANTENIMIENTO_SUSPENDIDA', 5.00, 'Cuota de mantenimiento mensual para conexiones suspendidas (Bs)')
ON CONFLICT (config_key) DO UPDATE 
SET config_value = EXCLUDED.config_value,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

-- Verificar los cambios
SELECT config_key, config_value, description 
FROM pos.billing_config 
WHERE config_key IN ('MULTA_CORTE', 'MANTENIMIENTO_SUSPENDIDA');
