-- Script para crear tabla de reuniones
-- Similar a jobs pero con campos de hora, minuto y AM/PM

CREATE TABLE IF NOT EXISTS pos.meeting (
    meeting_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    name varchar(255) NOT NULL,
    meeting_date date NOT NULL,
    hour integer NOT NULL CHECK (hour >= 1 AND hour <= 12),
    minute integer NOT NULL CHECK (minute >= 0 AND minute <= 59),
    am_pm varchar(2) NOT NULL CHECK (am_pm IN ('AM', 'PM')),
    description text,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz,
    active boolean DEFAULT true,
    CONSTRAINT pk_meeting PRIMARY KEY (meeting_id)
);

CREATE INDEX IF NOT EXISTS idx_meeting_active ON pos.meeting(active) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_meeting_date ON pos.meeting(meeting_date);

-- Comentarios para documentación
COMMENT ON TABLE pos.meeting IS 'Tabla de reuniones del sistema';
COMMENT ON COLUMN pos.meeting.hour IS 'Hora en formato 12 horas (1-12)';
COMMENT ON COLUMN pos.meeting.minute IS 'Minuto (0-59)';
COMMENT ON COLUMN pos.meeting.am_pm IS 'Indicador AM o PM';


