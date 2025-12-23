-- Script para hacer reader_user_id nullable en water_meter_reading
-- Esto permite registrar lecturas sin necesidad de un usuario específico

ALTER TABLE pos.water_meter_reading 
ALTER COLUMN reader_user_id DROP NOT NULL;

-- Comentario: Ahora las lecturas pueden registrarse usando solo el partnerId
-- El reader_user_id es opcional y puede ser NULL






