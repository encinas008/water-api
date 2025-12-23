-- Script para crear tabla de tipos de reunión

CREATE TABLE IF NOT EXISTS pos.meeting_type (
    meeting_type_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    code varchar(50) NOT NULL,
    name varchar(255) NOT NULL,
    active boolean DEFAULT true,
    CONSTRAINT pk_meeting_type PRIMARY KEY (meeting_type_id),
    CONSTRAINT uk_meeting_type_code UNIQUE (code)
);

-- Insertar tipos de reunión por defecto
INSERT INTO pos.meeting_type (code, name, active) VALUES
    ('AULL', 'AULL', true),
    ('CLASSIC', 'CLASICO', true)
ON CONFLICT (code) DO NOTHING;

-- Agregar columna meeting_type_id a la tabla meeting
ALTER TABLE pos.meeting
    ADD COLUMN IF NOT EXISTS meeting_type_id uuid;

-- Agregar foreign key
ALTER TABLE pos.meeting
    ADD CONSTRAINT fk_meeting_meeting_type
        FOREIGN KEY (meeting_type_id)
        REFERENCES pos.meeting_type(meeting_type_id)
        ON DELETE SET NULL;

-- Índice para búsquedas por tipo
CREATE INDEX IF NOT EXISTS idx_meeting_meeting_type_id 
    ON pos.meeting(meeting_type_id);

-- Comentarios para documentación
COMMENT ON TABLE pos.meeting_type IS 'Tipos de reunión disponibles en el sistema';
COMMENT ON COLUMN pos.meeting.meeting_type_id IS 'Tipo de reunión (AULL o CLASSIC)';

