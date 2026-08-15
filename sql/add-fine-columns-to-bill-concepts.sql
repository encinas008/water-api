-- Agregar columnas para enlazar conceptos de factura con las multas de asistencias a reuniones y trabajos
ALTER TABLE pos.bill_concept_item
ADD COLUMN fine_type VARCHAR(20) DEFAULT NULL,
ADD COLUMN fine_id UUID DEFAULT NULL;
