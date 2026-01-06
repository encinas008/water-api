-- ============================================
-- Tabla: billing_config
-- Descripción: Configuración de valores para facturación de agua
-- Autor: Sistema
-- Fecha: 2026-01-04
-- ============================================

-- Crear tabla billing_config
CREATE TABLE IF NOT EXISTS pos.billing_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Crear índice para búsquedas por config_key
CREATE INDEX IF NOT EXISTS idx_billing_config_key ON pos.billing_config(config_key);
CREATE INDEX IF NOT EXISTS idx_billing_config_active ON pos.billing_config(active);

-- Insertar valores por defecto
INSERT INTO pos.billing_config (config_key, config_value, description) VALUES
    ('APORTE_DEPORTE', 2.00, 'Monto fijo de aporte al deporte por factura (Bs)'),
    ('APORTE_OTB', 3.00, 'Monto fijo de aporte a la OTB por factura (Bs)'),
    ('TARIFA_BASICA', 15.00, 'Tarifa básica que incluye consumo hasta 15 m³ (Bs)'),
    ('MULTA_EXCESO_M3', 5.00, 'Multa por cada m³ de exceso sobre los 15 m³ base (Bs/m³)'),
    ('MULTA_CORTE', 50.00, 'Monto aplicado al cortar el servicio por mora acumulada (Bs)'),
    ('MANTENIMIENTO_SUSPENDIDA', 5.00, 'Cuota de mantenimiento mensual para conexiones suspendidas (Bs)')
ON CONFLICT (config_key) DO NOTHING;

-- Comentarios en las columnas
COMMENT ON TABLE pos.billing_config IS 'Configuración de valores para facturación de agua';
COMMENT ON COLUMN pos.billing_config.config_key IS 'Clave única de configuración';
COMMENT ON COLUMN pos.billing_config.config_value IS 'Valor numérico de la configuración';
COMMENT ON COLUMN pos.billing_config.description IS 'Descripción del parámetro de configuración';
COMMENT ON COLUMN pos.billing_config.active IS 'Indica si la configuración está activa';

-- Verificar datos insertados
SELECT * FROM pos.billing_config ORDER BY config_key;
