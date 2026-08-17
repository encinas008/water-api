BEGIN;

-- 1. Identificar todas las facturas y sus fechas de pago
CREATE TEMP TABLE temp_partner_bills AS
SELECT 
    b.water_bill_id, 
    b.partner_id, 
    b.billing_period_start, 
    st.code as current_status,
    b.active,
    (SELECT MAX(payment_date) FROM pos.water_payment p WHERE p.water_bill_id = b.water_bill_id) as last_payment_date
FROM pos.water_bill b
JOIN pos.bill_status_type st ON b.bill_status_type_id = st.bill_status_type_id
WHERE b.active = true;

-- 2. Determinar qué facturas estaban impagas al momento de generarse cada factura destino
CREATE TEMP TABLE temp_unpaid_at_time AS
SELECT 
    target_bill.water_bill_id as target_bill_id,
    target_bill.partner_id,
    target_bill.billing_period_start as target_date,
    prev_bill.water_bill_id as prev_bill_id,
    prev_bill.billing_period_start as prev_date,
    ROW_NUMBER() OVER(PARTITION BY target_bill.water_bill_id ORDER BY prev_bill.billing_period_start ASC) as rn
FROM temp_partner_bills target_bill
JOIN temp_partner_bills prev_bill 
  ON prev_bill.partner_id = target_bill.partner_id 
  AND prev_bill.billing_period_start < target_bill.billing_period_start
WHERE 
  (prev_bill.current_status IN ('PENDING', 'OVERDUE', 'PARTIAL_PAID')
   OR (prev_bill.current_status = 'PAID' AND prev_bill.last_payment_date >= target_bill.billing_period_start)
  );

-- 3. Contar cuántas facturas impagas previas tenía cada factura destino
CREATE TEMP TABLE temp_unpaid_counts AS
SELECT target_bill_id, partner_id, target_date, COUNT(prev_bill_id) as unpaid_count
FROM temp_unpaid_at_time
GROUP BY target_bill_id, partner_id, target_date;

-- 4. Filtrar aquellas facturas destino que cruzaron un múltiplo de 3 (tienen mora)
-- Solo queremos generar mora si el unpaid_count es un múltiplo exacto de 3.
-- EJ: 3, 6, 9. 
CREATE TEMP TABLE temp_mora_targets AS
SELECT target_bill_id, partner_id, target_date, unpaid_count
FROM temp_unpaid_counts
WHERE unpaid_count > 0 AND unpaid_count % 3 = 0;

-- 5. Generar los conceptos exactos para cada mora calculada
CREATE TEMP TABLE temp_new_moras AS
SELECT 
    mt.target_bill_id as water_bill_id,
    mt.partner_id,
    mt.target_date as assigned_date,
    50.00 as amount,
    'Multa por mora (' || 
        STRING_AGG(
            CASE EXTRACT(MONTH FROM uat.prev_date)
                WHEN 1 THEN 'ENERO' WHEN 2 THEN 'FEBRERO' WHEN 3 THEN 'MARZO'
                WHEN 4 THEN 'ABRIL' WHEN 5 THEN 'MAYO' WHEN 6 THEN 'JUNIO'
                WHEN 7 THEN 'JULIO' WHEN 8 THEN 'AGOSTO' WHEN 9 THEN 'SEPTIEMBRE'
                WHEN 10 THEN 'OCTUBRE' WHEN 11 THEN 'NOVIEMBRE' WHEN 12 THEN 'DICIEMBRE'
            END,
            '/' ORDER BY uat.prev_date ASC
        )
    || ')' as concept_name
FROM temp_mora_targets mt
JOIN temp_unpaid_at_time uat 
  ON mt.target_bill_id = uat.target_bill_id
-- Solo tomamos las 3 facturas que detonaron ESTA mora especifica
WHERE uat.rn > (mt.unpaid_count - 3) AND uat.rn <= mt.unpaid_count
GROUP BY mt.target_bill_id, mt.partner_id, mt.target_date;

-- 6. Limpiar todas las moras historicas (activos e inactivos)
CREATE TEMP TABLE temp_mora_to_delete AS
SELECT bill_concept_item_id, water_bill_id, amount, active
FROM pos.bill_concept_item
WHERE concept_name ILIKE '%mora%' OR concept_name ILIKE '%corte%' OR concept_name ILIKE '%recurrente%';

-- 7. Eliminar físicamente para evitar rastros sucios
DELETE FROM pos.bill_concept_item 
WHERE bill_concept_item_id IN (SELECT bill_concept_item_id FROM temp_mora_to_delete);

-- 8. Restar esos cobros eliminados de los totales de la factura
UPDATE pos.water_bill wb
SET 
    total_amount = total_amount - del.total_mora,
    remaining_balance = remaining_balance - del.total_mora
FROM (
    SELECT water_bill_id, SUM(amount) as total_mora
    FROM temp_mora_to_delete
    WHERE active = true
    GROUP BY water_bill_id
) del
WHERE wb.water_bill_id = del.water_bill_id;

-- 9. Insertar las nuevas moras saneadas en la base de datos
INSERT INTO pos.bill_concept_item (
    bill_concept_item_id,
    water_bill_id,
    concept_name,
    amount,
    assigned_date,
    active,
    created_at,
    updated_at
)
SELECT 
    gen_random_uuid(),
    water_bill_id,
    concept_name,
    amount,
    assigned_date,
    true,
    now(),
    now()
FROM temp_new_moras;

-- 10. Sumar el costo de las nuevas moras al total de sus facturas respectivas
UPDATE pos.water_bill wb
SET 
    total_amount = total_amount + nm.amount,
    remaining_balance = remaining_balance + nm.amount
FROM temp_new_moras nm
WHERE wb.water_bill_id = nm.water_bill_id;

-- 11. Actualizar la deuda global (current_debt) de los socios con la diferencia
CREATE TEMP TABLE temp_partner_diff AS
SELECT 
    p.partner_id,
    COALESCE(nm.total_new, 0) - COALESCE(om.total_old, 0) as diff
FROM pos.partner p
LEFT JOIN (
    SELECT partner_id, SUM(amount) as total_new FROM temp_new_moras GROUP BY partner_id
) nm ON p.partner_id = nm.partner_id
LEFT JOIN (
    SELECT tub.partner_id, SUM(tmd.amount) as total_old 
    FROM temp_mora_to_delete tmd 
    JOIN pos.water_bill tub ON tmd.water_bill_id = tub.water_bill_id
    WHERE tmd.active = true
    GROUP BY tub.partner_id
) om ON p.partner_id = om.partner_id
WHERE COALESCE(nm.total_new, 0) - COALESCE(om.total_old, 0) != 0;

UPDATE pos.partner p
SET current_debt = current_debt + tpd.diff
FROM temp_partner_diff tpd
WHERE p.partner_id = tpd.partner_id;

COMMIT;
