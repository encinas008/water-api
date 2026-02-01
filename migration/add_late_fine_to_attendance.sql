-- SQL para agregar la columna late_fine a la tabla meeting_attendance
-- Se utiliza NUMERIC(19, 2) para consistencia con los montos de multas y facturas
ALTER TABLE pos.meeting_attendance 
ADD COLUMN IF NOT EXISTS late_fine NUMERIC(19, 2) DEFAULT 0.00;

-- Comentario informativo
COMMENT ON COLUMN pos.meeting_attendance.late_fine IS 'Monto de la multa por retraso en la asistencia';
