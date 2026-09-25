WITH oldest_unpaid AS (
    SELECT 
        wb.partner_id,
        MIN(wb.billing_period_start) AS oldest_date
    FROM pos.water_bill wb
    JOIN pos.bill_status_type st ON wb.bill_status_type_id = st.bill_status_type_id
    WHERE wb.active = true 
      AND st.code IN ('PENDING', 'OVERDUE', 'PARTIAL_PAID')
    GROUP BY wb.partner_id
)
UPDATE pos.bill_concept_item bci
SET concept_name = 'Multa por mora (' || 
    CASE EXTRACT(MONTH FROM ou.oldest_date)
        WHEN 1 THEN 'ENERO'
        WHEN 2 THEN 'FEBRERO'
        WHEN 3 THEN 'MARZO'
        WHEN 4 THEN 'ABRIL'
        WHEN 5 THEN 'MAYO'
        WHEN 6 THEN 'JUNIO'
        WHEN 7 THEN 'JULIO'
        WHEN 8 THEN 'AGOSTO'
        WHEN 9 THEN 'SEPTIEMBRE'
        WHEN 10 THEN 'OCTUBRE'
        WHEN 11 THEN 'NOVIEMBRE'
        WHEN 12 THEN 'DICIEMBRE'
    END || '-' || EXTRACT(YEAR FROM ou.oldest_date) || ')'
FROM pos.water_bill wb
JOIN pos.bill_status_type st ON wb.bill_status_type_id = st.bill_status_type_id
JOIN oldest_unpaid ou ON wb.partner_id = ou.partner_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND st.code IN ('PENDING', 'OVERDUE', 'PARTIAL_PAID')
  AND (bci.concept_name ILIKE '%mora%' OR bci.concept_name ILIKE '%corte%' OR bci.concept_name ILIKE '%recurrente%')
  AND bci.concept_name NOT ILIKE 'Multa por mora (%';
