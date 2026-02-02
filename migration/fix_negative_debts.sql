/*
 * Script to fix negative debts for partners.
 * Equivalent to fix_negative_debts.py
 */

SET search_path TO pos;

BEGIN;

-- Check and display count of partners with negative debt before update
SELECT count(*) as count_before FROM partner WHERE current_debt < 0;

-- Update negative debts to 0
UPDATE partner 
SET current_debt = 0, 
    updated_at = NOW()
WHERE current_debt < 0;

-- Verify count after update (should be 0)
SELECT count(*) as count_after FROM partner WHERE current_debt < 0;

COMMIT;
