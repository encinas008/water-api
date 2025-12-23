-- Script para crear tabla de conceptos de cobro en facturas
-- Esto permite desglosar los cobros (Aporte al deporte, Tarifa básica, Aporte a la OTB)

CREATE TABLE IF NOT EXISTS pos.bill_concept_item (
    bill_concept_item_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    water_bill_id uuid NOT NULL,
    concept_name varchar(200) NOT NULL,
    assigned_date date NOT NULL,
    amount numeric(10, 2) NOT NULL,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz,
    active boolean DEFAULT true,
    CONSTRAINT pk_bill_concept_item PRIMARY KEY (bill_concept_item_id),
    CONSTRAINT fk_bill_concept_item_water_bill 
        FOREIGN KEY (water_bill_id) 
        REFERENCES pos.water_bill(water_bill_id) 
        ON DELETE CASCADE
);

-- Índice para búsquedas por factura
CREATE INDEX IF NOT EXISTS idx_bill_concept_item_water_bill_id 
    ON pos.bill_concept_item(water_bill_id);

-- Índice para búsquedas activas
CREATE INDEX IF NOT EXISTS idx_bill_concept_item_active 
    ON pos.bill_concept_item(active) 
    WHERE active = true;

