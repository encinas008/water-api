-- Script para agregar campo incremental único para socios
-- Este campo será el identificador autogenerado único del socio

-- Paso 1: Crear secuencia para números de socio
CREATE SEQUENCE IF NOT EXISTS pos.partner_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Paso 2: Agregar columna partner_number a la tabla partner
ALTER TABLE pos.partner
    ADD COLUMN IF NOT EXISTS partner_number BIGINT;

-- Paso 3: Crear índice único para asegurar que no se duplique
CREATE UNIQUE INDEX IF NOT EXISTS idx_partner_number_unique 
    ON pos.partner(partner_number) 
    WHERE partner_number IS NOT NULL;

-- Paso 4: Asignar números a socios existentes (si los hay)
-- Esto asigna números secuenciales basados en la fecha de creación
DO $$
DECLARE
    partner_record RECORD;
    counter BIGINT := 1;
BEGIN
    FOR partner_record IN 
        SELECT partner_id 
        FROM pos.partner 
        WHERE partner_number IS NULL 
        ORDER BY created_at ASC
    LOOP
        UPDATE pos.partner 
        SET partner_number = counter 
        WHERE partner_id = partner_record.partner_id;
        counter := counter + 1;
    END LOOP;
    
    -- Ajustar la secuencia al siguiente valor disponible
    IF counter > 1 THEN
        PERFORM setval('pos.partner_number_seq', counter - 1);
    END IF;
END $$;

-- Paso 5: Establecer valor por defecto para nuevos registros
ALTER TABLE pos.partner
    ALTER COLUMN partner_number SET DEFAULT nextval('pos.partner_number_seq');

-- Paso 6: Hacer la columna NOT NULL después de asignar valores a registros existentes
-- (Comentado por si hay registros NULL, descomentar después de verificar)
-- ALTER TABLE pos.partner ALTER COLUMN partner_number SET NOT NULL;

-- Comentarios para documentación
COMMENT ON COLUMN pos.partner.partner_number IS 'Número único incremental autogenerado del socio. Identificador visible en la UI.';
COMMENT ON SEQUENCE pos.partner_number_seq IS 'Secuencia para generar números únicos de socio';


