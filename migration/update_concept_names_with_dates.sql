-- ============================================================================
-- Migración: Agregar fecha a los conceptos de multas por Trabajo y Reunión
-- Fecha: 2026-05-01
-- Descripción: Actualiza los concept_name de bill_concept_item para incluir
--              la fecha de la asistencia con mes literal (ej: 15/Marzo/2026)
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

-- ============================================================================
-- 1. Actualizar conceptos de TRABAJOS
-- ============================================================================
UPDATE pos.bill_concept_item bci
SET concept_name = bci.concept_name || ' (' || TO_CHAR(ja.attendance_date, 'DD') || '/' || pos.mes_literal(ja.attendance_date) || '/' || TO_CHAR(ja.attendance_date, 'YYYY') || ')',
    updated_at = NOW()
FROM pos.water_bill wb
JOIN pos.job_attendance ja ON ja.partner_id = wb.partner_id
JOIN pos.job j ON j.job_id = ja.job_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND bci.concept_name LIKE 'Multa Trabajo:%'
  AND bci.concept_name NOT LIKE '%(%/%)'  -- Evitar doble aplicación
  AND bci.concept_name LIKE '%' || j.name || '%'
  AND ja.present = false
  AND ja.active = true;

-- ============================================================================
-- 2. Actualizar conceptos de REUNIONES
-- ============================================================================
UPDATE pos.bill_concept_item bci
SET concept_name = bci.concept_name || ' (' || TO_CHAR(ma.attendance_date, 'DD') || '/' || pos.mes_literal(ma.attendance_date) || '/' || TO_CHAR(ma.attendance_date, 'YYYY') || ')',
    updated_at = NOW()
FROM pos.water_bill wb
JOIN pos.meeting_attendance ma ON ma.partner_id = wb.partner_id
JOIN pos.meeting m ON m.meeting_id = ma.meeting_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND bci.concept_name LIKE 'Multa Reunión:%'
  AND bci.concept_name NOT LIKE '%(%/%)'  -- Evitar doble aplicación
  AND bci.concept_name LIKE '%' || m.name || '%'
  AND (ma.present = false OR ma.late_fine > 0)
  AND ma.active = true;

-- Limpiar función auxiliar (opcional, descomentar si no la necesitas después)
-- DROP FUNCTION IF EXISTS pos.mes_literal(DATE);
