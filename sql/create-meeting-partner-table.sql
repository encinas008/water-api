-- Script para crear tabla de relación entre reuniones y socios
-- Permite asignar múltiples socios a una reunión

CREATE TABLE IF NOT EXISTS pos.meeting_partner (
    meeting_partner_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    meeting_id uuid NOT NULL,
    partner_id uuid NOT NULL,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    active boolean DEFAULT true,
    CONSTRAINT pk_meeting_partner PRIMARY KEY (meeting_partner_id),
    CONSTRAINT fk_meeting_partner_meeting 
        FOREIGN KEY (meeting_id) 
        REFERENCES pos.meeting(meeting_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_meeting_partner_partner 
        FOREIGN KEY (partner_id) 
        REFERENCES pos.partner(partner_id) 
        ON DELETE CASCADE,
    CONSTRAINT uk_meeting_partner_unique UNIQUE (meeting_id, partner_id)
);

-- Índice para búsquedas por reunión
CREATE INDEX IF NOT EXISTS idx_meeting_partner_meeting_id 
    ON pos.meeting_partner(meeting_id);

-- Índice para búsquedas por socio
CREATE INDEX IF NOT EXISTS idx_meeting_partner_partner_id 
    ON pos.meeting_partner(partner_id);

-- Índice para búsquedas activas
CREATE INDEX IF NOT EXISTS idx_meeting_partner_active 
    ON pos.meeting_partner(active) 
    WHERE active = true;

