-- Script para renombrar la tabla attendance a job_attendance
-- y eliminar la tabla job_partner

-- Paso 1: Renombrar la tabla attendance a job_attendance
ALTER TABLE pos.attendance RENAME TO job_attendance;

-- Paso 2: Renombrar la secuencia si existe
ALTER SEQUENCE IF EXISTS pos.attendance_attendance_id_seq RENAME TO job_attendance_attendance_id_seq;

-- Paso 3: Renombrar índices si existen
ALTER INDEX IF EXISTS pos.idx_attendance_job_id RENAME TO idx_job_attendance_job_id;
ALTER INDEX IF EXISTS pos.idx_attendance_partner_id RENAME TO idx_job_attendance_partner_id;
ALTER INDEX IF EXISTS pos.idx_attendance_date RENAME TO idx_job_attendance_date;

-- Paso 4: Actualizar comentarios
COMMENT ON TABLE pos.job_attendance IS 'Registros de asistencia de socios a trabajos. Reemplaza a job_partner. Un socio está asignado a un trabajo si tiene un registro de asistencia activo.';

-- Paso 5: Eliminar foreign keys de job_partner si existen
ALTER TABLE pos.job_partner 
    DROP CONSTRAINT IF EXISTS fk_job_partner_job;

ALTER TABLE pos.job_partner 
    DROP CONSTRAINT IF EXISTS fk_job_partner_partner;

-- Paso 6: Eliminar índices de job_partner si existen
DROP INDEX IF EXISTS pos.idx_job_partner_job_id;
DROP INDEX IF EXISTS pos.idx_job_partner_partner_id;

-- Paso 7: Eliminar la tabla job_partner
DROP TABLE IF EXISTS pos.job_partner;

