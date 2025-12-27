-- Script para agregar columna de multa a las tablas de trabajos y reuniones

-- Agregar columna fine a la tabla job
ALTER TABLE pos.job
    ADD COLUMN IF NOT EXISTS fine NUMERIC(19, 2);

-- Agregar columna fine a la tabla meeting
ALTER TABLE pos.meeting
    ADD COLUMN IF NOT EXISTS fine NUMERIC(19, 2);

-- Comentarios para documentación
COMMENT ON COLUMN pos.job.fine IS 'Multa asociada al trabajo (opcional)';
COMMENT ON COLUMN pos.meeting.fine IS 'Multa asociada a la reunión (opcional)';


