-- Script para crear tabla de relación entre trabajos y socios
-- Permite asignar múltiples socios a un trabajo

CREATE TABLE IF NOT EXISTS pos.job_partner (
    job_partner_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    job_id uuid NOT NULL,
    partner_id uuid NOT NULL,
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    active boolean DEFAULT true,
    CONSTRAINT pk_job_partner PRIMARY KEY (job_partner_id),
    CONSTRAINT fk_job_partner_job 
        FOREIGN KEY (job_id) 
        REFERENCES pos.job(job_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_job_partner_partner 
        FOREIGN KEY (partner_id) 
        REFERENCES pos.partner(partner_id) 
        ON DELETE CASCADE,
    CONSTRAINT uk_job_partner_unique UNIQUE (job_id, partner_id)
);

-- Índice para búsquedas por trabajo
CREATE INDEX IF NOT EXISTS idx_job_partner_job_id 
    ON pos.job_partner(job_id);

-- Índice para búsquedas por socio
CREATE INDEX IF NOT EXISTS idx_job_partner_partner_id 
    ON pos.job_partner(partner_id);

-- Índice para búsquedas activas
CREATE INDEX IF NOT EXISTS idx_job_partner_active 
    ON pos.job_partner(active) 
    WHERE active = true;

