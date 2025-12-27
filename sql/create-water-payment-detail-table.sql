-- Script para crear la tabla water_payment_detail

CREATE TABLE IF NOT EXISTS pos.water_payment_detail (
    water_payment_detail_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    water_payment_id UUID NOT NULL,
    fine_type VARCHAR(20) NOT NULL, -- 'JOB' o 'MEETING'
    fine_id UUID NOT NULL, -- ID de la ausencia (JobAttendance o MeetingAttendance)
    fine_name VARCHAR(255) NOT NULL, -- Nombre del trabajo o reunión
    fine_date DATE NOT NULL, -- Fecha de la ausencia
    fine_amount NUMERIC(19, 2) NOT NULL, -- Monto de la multa
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_water_payment_detail_payment 
        FOREIGN KEY (water_payment_id) 
        REFERENCES pos.water_payment(water_payment_id) 
        ON DELETE CASCADE,
    
    CONSTRAINT uq_water_payment_detail_payment_fine 
        UNIQUE (water_payment_id, fine_type, fine_id),
    
    CONSTRAINT chk_fine_type CHECK (fine_type IN ('JOB', 'MEETING'))
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_water_payment_detail_payment_id 
    ON pos.water_payment_detail(water_payment_id);

CREATE INDEX IF NOT EXISTS idx_water_payment_detail_fine_id 
    ON pos.water_payment_detail(fine_id);

CREATE INDEX IF NOT EXISTS idx_water_payment_detail_fine_type 
    ON pos.water_payment_detail(fine_type);

-- Comentarios
COMMENT ON TABLE pos.water_payment_detail IS 'Detalle de multas incluidas en un pago de agua';
COMMENT ON COLUMN pos.water_payment_detail.water_payment_id IS 'Referencia al pago de agua';
COMMENT ON COLUMN pos.water_payment_detail.fine_type IS 'Tipo de multa: JOB (trabajo) o MEETING (reunión)';
COMMENT ON COLUMN pos.water_payment_detail.fine_id IS 'ID de la ausencia que generó la multa';
COMMENT ON COLUMN pos.water_payment_detail.fine_name IS 'Nombre del trabajo o reunión';
COMMENT ON COLUMN pos.water_payment_detail.fine_date IS 'Fecha de la ausencia';
COMMENT ON COLUMN pos.water_payment_detail.fine_amount IS 'Monto de la multa';


