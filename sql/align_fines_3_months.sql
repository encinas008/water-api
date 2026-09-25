WITH numbered_unpaid AS (
    SELECT 
        wb.partner_id,
        wb.billing_period_start,
        ROW_NUMBER() OVER(PARTITION BY wb.partner_id ORDER BY wb.billing_period_start ASC) as rn
    FROM pos.water_bill wb
    JOIN pos.bill_status_type st ON wb.bill_status_type_id = st.bill_status_type_id
    WHERE wb.active = true 
      AND wb.billing_period_start >= '2026-01-01'
      AND st.code IN ('PENDING', 'OVERDUE', 'PARTIAL_PAID')
),
oldest_3 AS (
    SELECT 
        partner_id,
        STRING_AGG(
            CASE EXTRACT(MONTH FROM billing_period_start)
                WHEN 1 THEN 'ENERO' WHEN 2 THEN 'FEBRERO' WHEN 3 THEN 'MARZO'
                WHEN 4 THEN 'ABRIL' WHEN 5 THEN 'MAYO' WHEN 6 THEN 'JUNIO'
                WHEN 7 THEN 'JULIO' WHEN 8 THEN 'AGOSTO' WHEN 9 THEN 'SEPTIEMBRE'
                WHEN 10 THEN 'OCTUBRE' WHEN 11 THEN 'NOVIEMBRE' WHEN 12 THEN 'DICIEMBRE'
            END,
            '/' ORDER BY billing_period_start ASC
        ) as months_str
    FROM numbered_unpaid
    WHERE rn <= 3
    GROUP BY partner_id
)
UPDATE pos.bill_concept_item bci
SET concept_name = 'Multa por mora (' || o3.months_str || ')'
FROM pos.water_bill wb
JOIN pos.bill_status_type st ON wb.bill_status_type_id = st.bill_status_type_id
JOIN oldest_3 o3 ON wb.partner_id = o3.partner_id
WHERE bci.water_bill_id = wb.water_bill_id
  AND bci.active = true
  AND st.code IN ('PENDING', 'OVERDUE', 'PARTIAL_PAID')
  AND bci.concept_name ILIKE 'Multa por mora (%-%)';
