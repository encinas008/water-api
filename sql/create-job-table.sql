-- Script para crear tabla de trabajos (jobs)
-- Administración de trabajos con nombre, fecha de inicio y descripción

CREATE TABLE IF NOT EXISTS pos.job (
    job_id uuid DEFAULT uuid_generate_v4() NOT NULL,
    name varchar(200) NOT NULL,
    start_date date NOT NULL,
    description text DEFAULT '',
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz,
    active boolean DEFAULT true,
    CONSTRAINT pk_job PRIMARY KEY (job_id)
);

-- Índice para búsquedas por nombre
CREATE INDEX IF NOT EXISTS idx_job_name 
    ON pos.job(name);

-- Índice para búsquedas por fecha de inicio
CREATE INDEX IF NOT EXISTS idx_job_start_date 
    ON pos.job(start_date);

-- Índice para búsquedas activas
CREATE INDEX IF NOT EXISTS idx_job_active 
    ON pos.job(active) 
    WHERE active = true;

-- Índice para ordenamiento por fecha de creación
CREATE INDEX IF NOT EXISTS idx_job_created_at 
    ON pos.job(created_at DESC);



