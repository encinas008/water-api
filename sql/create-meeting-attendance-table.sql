-- Script para crear tabla de asistencia de socios a reuniones

CREATE TABLE IF NOT EXISTS pos.meeting_attendance (
    meeting_attendance_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    meeting_id uuid NOT NULL,
    partner_id uuid NOT NULL,
    attendance_date date NOT NULL,
    present boolean DEFAULT false NOT NULL,
    check_in_time timestamptz,
    check_out_time timestamptz,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz,
    active boolean DEFAULT true,
    CONSTRAINT pk_meeting_attendance PRIMARY KEY (meeting_attendance_id),
    CONSTRAINT fk_meeting_attendance_meeting 
        FOREIGN KEY (meeting_id) 
        REFERENCES pos.meeting(meeting_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_meeting_attendance_partner 
        FOREIGN KEY (partner_id) 
        REFERENCES pos.partner(partner_id) 
        ON DELETE CASCADE,
    CONSTRAINT uk_meeting_attendance_unique UNIQUE (meeting_id, partner_id, attendance_date)
);

-- Índices para búsquedas
CREATE INDEX IF NOT EXISTS idx_meeting_attendance_meeting_id 
    ON pos.meeting_attendance(meeting_id);

CREATE INDEX IF NOT EXISTS idx_meeting_attendance_partner_id 
    ON pos.meeting_attendance(partner_id);

CREATE INDEX IF NOT EXISTS idx_meeting_attendance_date 
    ON pos.meeting_attendance(attendance_date);

CREATE INDEX IF NOT EXISTS idx_meeting_attendance_active 
    ON pos.meeting_attendance(active) 
    WHERE active = true;

