-- ============================================================================
-- FIX: Desactivar conceptos de multa duplicados en facturas
-- Fecha: 2026-09-14
-- Descripción: Algunos conceptos de multa (Trabajo/Reunión) se insertaron
--              duplicados con diferencias de espacios en el nombre.
--              Este script desactiva el duplicado más reciente y ajusta
--              los montos de la factura y la deuda del socio.
-- Afectados: 24 conceptos duplicados, Bs 2,850.00 de cobro de más
-- ============================================================================

BEGIN;

-- ============================================================================
-- Paso 1: Identificar duplicados (conservar el más antiguo, marcar el más nuevo)
-- ============================================================================
WITH duplicates AS (
    SELECT
        bci.bill_concept_item_id,
        bci.water_bill_id,
        bci.amount,
        ROW_NUMBER() OVER (
            PARTITION BY
                bci.water_bill_id,
                TRIM(REGEXP_REPLACE(bci.concept_name, '\s+', ' ', 'g'))
            ORDER BY bci.created_at ASC
        ) AS rn
    FROM pos.bill_concept_item bci
    WHERE bci.active = true
      AND (bci.concept_name LIKE 'Multa Trabajo:%'
           OR bci.concept_name LIKE 'Multa Reunión:%')
)

-- Paso 2: Desactivar los conceptos duplicados (rn > 1 = no es el primero)
UPDATE pos.bill_concept_item
SET active = false,
    updated_at = NOW()
WHERE bill_concept_item_id IN (
    SELECT bill_concept_item_id FROM duplicates WHERE rn > 1
);

-- ============================================================================
-- Paso 3: Recalcular total_amount y remaining_balance de las facturas afectadas
-- ============================================================================
WITH bill_totals AS (
    SELECT
        bci.water_bill_id,
        SUM(bci.amount) AS sum_concepts
    FROM pos.bill_concept_item bci
    WHERE bci.active = true
    GROUP BY bci.water_bill_id
)
UPDATE pos.water_bill wb
SET total_amount       = bt.sum_concepts,
    remaining_balance  = GREATEST(bt.sum_concepts - wb.paid_amount, 0),
    updated_at         = NOW()
FROM bill_totals bt
WHERE wb.water_bill_id = bt.water_bill_id
  AND wb.active = true
  AND wb.water_bill_id IN (
      -- Solo actualizar facturas que tenían duplicados
      SELECT DISTINCT bci.water_bill_id
      FROM pos.bill_concept_item bci
      WHERE bci.active = false
        AND bci.updated_at >= NOW() - INTERVAL '1 minute'
        AND (bci.concept_name LIKE 'Multa Trabajo:%'
             OR bci.concept_name LIKE 'Multa Reunión:%')
  );

-- ============================================================================
-- Paso 4: Recalcular current_debt de los socios afectados
-- ============================================================================
WITH partner_debts AS (
    SELECT
        wb.partner_id,
        COALESCE(SUM(wb.remaining_balance), 0) AS total_debt
    FROM pos.water_bill wb
    WHERE wb.active = true
    GROUP BY wb.partner_id
)
UPDATE pos.partner p
SET current_debt = pd.total_debt,
    updated_at   = NOW()
FROM partner_debts pd
WHERE p.partner_id = pd.partner_id
  AND p.partner_id IN (
      SELECT DISTINCT wb.partner_id
      FROM pos.water_bill wb
      JOIN pos.bill_concept_item bci ON bci.water_bill_id = wb.water_bill_id
      WHERE bci.active = false
        AND bci.updated_at >= NOW() - INTERVAL '1 minute'
        AND (bci.concept_name LIKE 'Multa Trabajo:%'
             OR bci.concept_name LIKE 'Multa Reunión:%')
  );

-- ============================================================================
-- Paso 5: Verificación - mostrar resultado final
-- ============================================================================
SELECT
    p.partner_number AS nro_socio,
    p.full_name      AS socio,
    wb.bill_number   AS nro_factura,
    wb.total_amount  AS total_factura_nuevo,
    wb.remaining_balance AS saldo_nuevo,
    p.current_debt   AS deuda_socio_nueva
FROM pos.water_bill wb
JOIN pos.partner p ON p.partner_id = wb.partner_id
WHERE wb.water_bill_id IN (
    SELECT DISTINCT bci.water_bill_id
    FROM pos.bill_concept_item bci
    WHERE bci.active = false
      AND bci.updated_at >= NOW() - INTERVAL '1 minute'
      AND (bci.concept_name LIKE 'Multa Trabajo:%'
           OR bci.concept_name LIKE 'Multa Reunión:%')
)
ORDER BY p.partner_number;

COMMIT;
