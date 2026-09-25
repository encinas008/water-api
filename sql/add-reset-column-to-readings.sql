-- Marca de reinicio de contador en lecturas de medidor.
-- Cuando is_reset = true, la lectura registrada inicia su conteo desde 0
-- (ej. medidor reemplazado) y el motivo se guarda en la observación de la lectura.
ALTER TABLE pos.water_meter_reading
    ADD COLUMN IF NOT EXISTS is_reset BOOLEAN DEFAULT false;