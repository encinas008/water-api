-- Script para crear tabla de asistencia de socios a trabajos
-- Permite registrar la asistencia diaria de cada socio a un trabajo específico

CREATE TABLE IF NOT EXISTS pos.attendance (
    attendance_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    job_id uuid NOT NULL,
    partner_id uuid NOT NULL,
    attendance_date date NOT NULL,
    present boolean DEFAULT false NOT NULL,
    check_in_time timestamptz,
    check_out_time timestamptz,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz,
    active boolean DEFAULT true,
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT fk_attendance_job
        FOREIGN KEY (job_id)
        REFERENCES pos.job(job_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_attendance_partner
        FOREIGN KEY (partner_id)
        REFERENCES pos.partner(partner_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_attendance_unique UNIQUE (job_id, partner_id, attendance_date)
);

-- Índices para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_attendance_job_id
    ON pos.attendance(job_id);

CREATE INDEX IF NOT EXISTS idx_attendance_partner_id
    ON pos.attendance(partner_id);

CREATE INDEX IF NOT EXISTS idx_attendance_date
    ON pos.attendance(attendance_date);

CREATE INDEX IF NOT EXISTS idx_attendance_job_date
    ON pos.attendance(job_id, attendance_date);

-- Índice para búsquedas activas
CREATE INDEX IF NOT EXISTS idx_attendance_active
    ON pos.attendance(active)
    WHERE active = true;

-- Comentarios para documentación
COMMENT ON TABLE pos.attendance IS 'Registro de asistencia de socios a trabajos';
COMMENT ON COLUMN pos.attendance.attendance_date IS 'Fecha de la asistencia';
COMMENT ON COLUMN pos.attendance.present IS 'Indica si el socio asistió (true) o faltó (false)';
COMMENT ON COLUMN pos.attendance.check_in_time IS 'Hora de entrada del socio';
COMMENT ON COLUMN pos.attendance.check_out_time IS 'Hora de salida del socio';


