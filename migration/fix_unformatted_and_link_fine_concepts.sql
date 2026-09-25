-- ============================================================================
-- Migración: Estandarizar conceptos de multas (Reunión y Trabajo) y vincular fine_id
-- Fecha: 2026-09-13
-- Descripción:
--   1. Agrega la fecha en formato literal (DD/Mes/YYYY) a todos los conceptos de
--      multas de reuniones (incluyendo 'Multa AULL:%' y otras reuniones) que no
--      tenían fecha.
--   2. Vincula fine_id y fine_type en pos.bill_concept_item hacia pos.meeting_attendance.
--   3. Agrega la fecha y vincula fine_id/fine_type en conceptos de trabajos hacia
--      pos.job_attendance.
-- ============================================================================

-- Función auxiliar para obtener el nombre del mes en español
CREATE OR REPLACE FUNCTION pos.mes_literal(fecha DATE) RETURNS TEXT AS $$
BEGIN
    RETURN CASE EXTRACT(MONTH FROM fecha)
        WHEN 1  THEN 'Enero'
        WHEN 2  THEN 'Febrero'
        WHEN 3  THEN 'Marzo'
        WHEN 4  THEN 'Abril'
        WHEN 5  THEN 'Mayo'
        WHEN 6  THEN 'Junio'
        WHEN 7  THEN 'Julio'
        WHEN 8  THEN 'Agosto'
        WHEN 9  THEN 'Septiembre'
        WHEN 10 THEN 'Octubre'
        WHEN 11 THEN 'Noviembre'
        WHEN 12 THEN 'Diciembre'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

BEGIN;

-- 1. Actualizar conceptos de REUNIONES sin fecha y vincular fine_id/fine_type
UPDATE pos.bill_concept_item bci
SET concept_name = bci.concept_name || ' (' || TO_CHAR(ma.attendance_date, 'DD') || '/' || pos.mes_literal(ma.attendance_date) || '/' || TO_CHAR(ma.attendance_date, 'YYYY') || ')',
    fine_id = ma.meeting_attendance_id,
    fine_type = 'MEETING',
    updated_at = NOW()
FROM pos.water_bill wb
JOIN pos.meeting_attendance ma ON ma.partner_id = wb.partner_id
  AND EXTRACT(MONTH FROM ma.attendance_date) = EXTRACT(MONTH FROM wb.billing_period_start)
  AND EXTRACT(YEAR FROM ma.attendance_date) = EXTRACT(YEAR FROM wb.billing_period_start)
JOIN pos.meeting m ON m.meeting_id = ma.meeting_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND (bci.concept_name LIKE 'Multa AULL:%' OR bci.concept_name LIKE 'Multa Reunión:%' OR bci.concept_name LIKE 'Multa %')
  AND bci.concept_name NOT LIKE '%(%/%)'
  AND bci.concept_name ILIKE '%' || m.name || '%'
  AND (ma.present = false OR ma.late_fine > 0)
  AND ma.active = true;

-- 2. Vincular fine_id en conceptos de REUNIONES que ya tenían fecha pero fine_id IS NULL
UPDATE pos.bill_concept_item bci
SET fine_id = ma.meeting_attendance_id,
    fine_type = 'MEETING',
    updated_at = NOW()
FROM pos.water_bill wb
JOIN pos.meeting_attendance ma ON ma.partner_id = wb.partner_id
  AND EXTRACT(MONTH FROM ma.attendance_date) = EXTRACT(MONTH FROM wb.billing_period_start)
  AND EXTRACT(YEAR FROM ma.attendance_date) = EXTRACT(YEAR FROM wb.billing_period_start)
JOIN pos.meeting m ON m.meeting_id = ma.meeting_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND bci.fine_id IS NULL
  AND (bci.concept_name LIKE 'Multa AULL:%' OR bci.concept_name LIKE 'Multa Reunión:%' OR bci.concept_name LIKE 'Multa %')
  AND bci.concept_name ILIKE '%' || m.name || '%'
  AND (ma.present = false OR ma.late_fine > 0)
  AND ma.active = true;

-- 3. Actualizar conceptos de TRABAJOS sin fecha y vincular fine_id/fine_type
UPDATE pos.bill_concept_item bci
SET concept_name = bci.concept_name || ' (' || TO_CHAR(ja.attendance_date, 'DD') || '/' || pos.mes_literal(ja.attendance_date) || '/' || TO_CHAR(ja.attendance_date, 'YYYY') || ')',
    fine_id = ja.attendance_id,
    fine_type = 'JOB',
    updated_at = NOW()
FROM pos.water_bill wb
JOIN pos.job_attendance ja ON ja.partner_id = wb.partner_id
  AND EXTRACT(MONTH FROM ja.attendance_date) = EXTRACT(MONTH FROM wb.billing_period_start)
  AND EXTRACT(YEAR FROM ja.attendance_date) = EXTRACT(YEAR FROM wb.billing_period_start)
JOIN pos.job j ON j.job_id = ja.job_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND bci.concept_name LIKE 'Multa Trabajo:%'
  AND bci.concept_name NOT LIKE '%(%/%)'
  AND bci.concept_name ILIKE '%' || j.name || '%'
  AND ja.present = false
  AND ja.active = true;

-- 4. Vincular fine_id en conceptos de TRABAJOS que ya tenían fecha pero fine_id IS NULL
UPDATE pos.bill_concept_item bci
SET fine_id = ja.attendance_id,
    fine_type = 'JOB',
    updated_at = NOW()
FROM pos.water_bill wb
JOIN pos.job_attendance ja ON ja.partner_id = wb.partner_id
  AND EXTRACT(MONTH FROM ja.attendance_date) = EXTRACT(MONTH FROM wb.billing_period_start)
  AND EXTRACT(YEAR FROM ja.attendance_date) = EXTRACT(YEAR FROM wb.billing_period_start)
JOIN pos.job j ON j.job_id = ja.job_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND bci.fine_id IS NULL
  AND bci.concept_name LIKE 'Multa Trabajo:%'
  AND bci.concept_name ILIKE '%' || j.name || '%'
  AND ja.present = false
  AND ja.active = true;

COMMIT;
