BEGIN;

-- 1. Identify all unpaid bills
CREATE TEMP TABLE temp_unpaid_bills AS
SELECT 
    wb.water_bill_id,
    wb.partner_id,
    wb.billing_period_start,
    wb.base_amount,
    wb.total_amount,
    wb.remaining_balance,
    wb.paid_amount,
    ROW_NUMBER() OVER(PARTITION BY wb.partner_id ORDER BY wb.billing_period_start ASC) as rn
FROM pos.water_bill wb
JOIN pos.bill_status_type st ON wb.bill_status_type_id = st.bill_status_type_id
WHERE wb.active = true 
  AND st.code IN ('PENDING', 'OVERDUE', 'PARTIAL_PAID');

-- 2. Identify all mora concepts to delete
CREATE TEMP TABLE temp_mora_to_delete AS
SELECT bci.bill_concept_item_id, bci.water_bill_id, bci.amount
FROM pos.bill_concept_item bci
JOIN temp_unpaid_bills tub ON bci.water_bill_id = tub.water_bill_id
WHERE bci.active = true
  AND (bci.concept_name ILIKE '%mora%' OR bci.concept_name ILIKE '%corte%' OR bci.concept_name ILIKE '%recurrente%');

-- 3. Delete them logically (or physically, but let's do logically to match active=false or just delete)
DELETE FROM pos.bill_concept_item 
WHERE bill_concept_item_id IN (SELECT bill_concept_item_id FROM temp_mora_to_delete);

-- 4. Update the bill totals by subtracting the deleted amounts
UPDATE pos.water_bill wb
SET 
    total_amount = total_amount - del.total_mora,
    remaining_balance = remaining_balance - del.total_mora
FROM (
    SELECT water_bill_id, SUM(amount) as total_mora
    FROM temp_mora_to_delete
    GROUP BY water_bill_id
) del
WHERE wb.water_bill_id = del.water_bill_id;

-- 5. Calculate new mora concepts
-- We need to attach a mora fine to bills where rn % 3 = 1 AND rn > 1 (so rn=4, 7, 10...)
-- The fine is for the previous 3 months.
CREATE TEMP TABLE temp_new_moras AS
SELECT 
    target_bill.water_bill_id,
    target_bill.partner_id,
    target_bill.billing_period_start as assigned_date,
    50.00 as amount,
    'Multa por mora (' || 
        STRING_AGG(
            CASE EXTRACT(MONTH FROM prev_bills.billing_period_start)
                WHEN 1 THEN 'ENERO' WHEN 2 THEN 'FEBRERO' WHEN 3 THEN 'MARZO'
                WHEN 4 THEN 'ABRIL' WHEN 5 THEN 'MAYO' WHEN 6 THEN 'JUNIO'
                WHEN 7 THEN 'JULIO' WHEN 8 THEN 'AGOSTO' WHEN 9 THEN 'SEPTIEMBRE'
                WHEN 10 THEN 'OCTUBRE' WHEN 11 THEN 'NOVIEMBRE' WHEN 12 THEN 'DICIEMBRE'
            END,
            '/' ORDER BY prev_bills.billing_period_start ASC
        )
    || ')' as concept_name
FROM temp_unpaid_bills target_bill
JOIN temp_unpaid_bills prev_bills 
  ON prev_bills.partner_id = target_bill.partner_id 
  AND prev_bills.rn >= target_bill.rn - 3 
  AND prev_bills.rn < target_bill.rn
WHERE target_bill.rn > 1 AND target_bill.rn % 3 = 1
GROUP BY target_bill.water_bill_id, target_bill.partner_id, target_bill.billing_period_start;

-- 6. Insert new moras
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

-- 7. Update the bill totals for the newly inserted moras
UPDATE pos.water_bill wb
SET 
    total_amount = total_amount + nm.amount,
    remaining_balance = remaining_balance + nm.amount
FROM temp_new_moras nm
WHERE wb.water_bill_id = nm.water_bill_id;

-- 8. Also update the partner's current_debt!
-- We need to recalculate partner current_debt. Easiest way is to recalculate from scratch.
-- But we can just do: current_debt = sum of remaining_balance of unpaid bills + pending fines from other tables.
-- Actually, the backend calculates it when needed. We'll just update it by the net difference.
-- Net difference per partner = (new mora amounts) - (old mora amounts)

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
    JOIN temp_unpaid_bills tub ON tmd.water_bill_id = tub.water_bill_id
    GROUP BY tub.partner_id
) om ON p.partner_id = om.partner_id
WHERE COALESCE(nm.total_new, 0) - COALESCE(om.total_old, 0) != 0;

UPDATE pos.partner p
SET current_debt = current_debt + tpd.diff
FROM temp_partner_diff tpd
WHERE p.partner_id = tpd.partner_id;

COMMIT;
