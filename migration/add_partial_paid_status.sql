-- SQL para agregar el estado PARTIAL_PAID si no existe
INSERT INTO pos.bill_status_type (bill_status_type_id, code, name, description, active, created_at)
SELECT 
    'f3e2b1a0-d9c8-4b7a-a521-e8d9c0a1b2c3', -- UUID generado para consistencia
    'PARTIAL_PAID', 
    'PAGO PARCIAL', 
    'Factura de Pago Parcial', 
    true, 
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM pos.bill_status_type WHERE code = 'PARTIAL_PAID'
);

-- Verificar los estados actuales
SELECT code, name, description FROM pos.bill_status_type;
