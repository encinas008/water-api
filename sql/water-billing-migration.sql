-- =====================================================
-- WATER BILLING SYSTEM - DATABASE MIGRATION SCRIPT
-- =====================================================
-- This script migrates the client table to partner table
-- and adds water billing system functionality
-- =====================================================

-- Step 1: Rename client table to partner
ALTER TABLE pos.client RENAME TO partner;

-- Step 2: Rename client columns to partner columns
ALTER TABLE pos.partner RENAME COLUMN client_id TO partner_id;
ALTER TABLE pos.partner RENAME COLUMN client_identification_number TO partner_identification_number;

-- Step 3: Add water connection fields to partner table
ALTER TABLE pos.partner
    ADD COLUMN water_connection_number varchar(50),
    ADD COLUMN water_meter_number varchar(50),
    ADD COLUMN connection_status_type_id uuid,
    ADD COLUMN connection_date date,
    ADD COLUMN water_connection_address varchar(300),
    ADD COLUMN current_debt numeric DEFAULT 0,
    ADD COLUMN last_billing_date date,
    ADD COLUMN notes varchar(500) DEFAULT '';

-- Step 4: Create connection_status_type catalog table
CREATE TABLE pos.connection_status_type (
                                            connection_status_type_id uuid DEFAULT uuid_generate_v4() NOT NULL,
                                            code varchar NOT NULL,
                                            name varchar(100) NOT NULL,
                                            description varchar,
                                            created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
                                            updated_at timestamptz,
                                            active boolean DEFAULT true,
                                            CONSTRAINT pk_connection_status_type PRIMARY KEY (connection_status_type_id),
                                            CONSTRAINT unq_connection_status_type UNIQUE (code, name)
);

-- Step 5: Create bill_status_type catalog table
CREATE TABLE pos.bill_status_type (
                                      bill_status_type_id uuid DEFAULT uuid_generate_v4() NOT NULL,
                                      code varchar NOT NULL,
                                      name varchar(100) NOT NULL,
                                      description varchar,
                                      created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
                                      updated_at timestamptz,
                                      active boolean DEFAULT true,
                                      CONSTRAINT pk_bill_status_type PRIMARY KEY (bill_status_type_id),
                                      CONSTRAINT unq_bill_status_type UNIQUE (code, name)
);

-- Step 6: Create water_meter_reading table
CREATE TABLE pos.water_meter_reading (
                                         reading_id uuid DEFAULT uuid_generate_v4() NOT NULL,
                                         partner_id uuid NOT NULL,
                                         reading_date date NOT NULL,
                                         previous_reading numeric NOT NULL,
                                         current_reading numeric NOT NULL,
                                         consumption numeric NOT NULL,
                                         reader_user_id uuid NOT NULL,
                                         observation varchar(500) DEFAULT '',
                                         image_id uuid,
                                         created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
                                         updated_at timestamptz,
                                         active boolean DEFAULT true,
                                         CONSTRAINT pk_water_meter_reading PRIMARY KEY (reading_id)
);

-- Step 7: Create water_bill table
CREATE TABLE pos.water_bill (
                                water_bill_id uuid DEFAULT uuid_generate_v4() NOT NULL,
                                bill_number varchar(100) NOT NULL,
                                partner_id uuid NOT NULL,
                                reading_id uuid,
                                billing_period_start date NOT NULL,
                                billing_period_end date NOT NULL,
                                consumption_m3 numeric NOT NULL,
                                rate_per_m3 numeric NOT NULL,
                                base_amount numeric NOT NULL,
                                total_amount numeric NOT NULL,
                                paid_amount numeric DEFAULT 0,
                                remaining_balance numeric NOT NULL,
                                bill_status_type_id uuid NOT NULL,
                                due_date date NOT NULL,
                                paid_date date,
                                created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
                                updated_at timestamptz,
                                active boolean DEFAULT true,
                                CONSTRAINT pk_water_bill PRIMARY KEY (water_bill_id),
                                CONSTRAINT unq_water_bill_number UNIQUE (bill_number)
);

-- Step 8: Create water_payment table
CREATE TABLE pos.water_payment (
                                   water_payment_id uuid DEFAULT uuid_generate_v4() NOT NULL,
                                   water_bill_id uuid NOT NULL,
                                   partner_id uuid NOT NULL,
                                   payment_date date NOT NULL,
                                   amount numeric NOT NULL,
                                   payment_type_id uuid NOT NULL,
                                   cash_balance_id uuid,
                                   user_id uuid NOT NULL,
                                   receipt_number varchar(100) NOT NULL,
                                   observation varchar(500) DEFAULT '',
                                   created_at timestamptz DEFAULT CURRENT_TIMESTAMP,
                                   updated_at timestamptz,
                                   active boolean DEFAULT true,
                                   CONSTRAINT pk_water_payment PRIMARY KEY (water_payment_id),
                                   CONSTRAINT unq_water_payment_receipt UNIQUE (receipt_number)
);

-- Step 9: Update foreign key in sale table
ALTER TABLE pos.sale DROP CONSTRAINT IF EXISTS fk_sale_client;
ALTER TABLE pos.sale RENAME COLUMN client_id TO partner_id;

-- Step 10: Add foreign key constraints
ALTER TABLE pos.partner
    ADD CONSTRAINT fk_partner_connection_status
        FOREIGN KEY (connection_status_type_id)
            REFERENCES pos.connection_status_type(connection_status_type_id);

ALTER TABLE pos.sale
    ADD CONSTRAINT fk_sale_partner
        FOREIGN KEY (partner_id)
            REFERENCES pos.partner(partner_id);

ALTER TABLE pos.water_meter_reading
    ADD CONSTRAINT fk_water_reading_partner
        FOREIGN KEY (partner_id)
            REFERENCES pos.partner(partner_id);

ALTER TABLE pos.water_meter_reading
    ADD CONSTRAINT fk_water_reading_user
        FOREIGN KEY (reader_user_id)
            REFERENCES pos.abac_user(user_id);

ALTER TABLE pos.water_meter_reading
    ADD CONSTRAINT fk_water_reading_image
        FOREIGN KEY (image_id)
            REFERENCES pos.image(image_id);

ALTER TABLE pos.water_bill
    ADD CONSTRAINT fk_water_bill_partner
        FOREIGN KEY (partner_id)
            REFERENCES pos.partner(partner_id);

ALTER TABLE pos.water_bill
    ADD CONSTRAINT fk_water_bill_reading
        FOREIGN KEY (reading_id)
            REFERENCES pos.water_meter_reading(reading_id);

ALTER TABLE pos.water_bill
    ADD CONSTRAINT fk_water_bill_status
        FOREIGN KEY (bill_status_type_id)
            REFERENCES pos.bill_status_type(bill_status_type_id);

ALTER TABLE pos.water_payment
    ADD CONSTRAINT fk_water_payment_bill
        FOREIGN KEY (water_bill_id)
            REFERENCES pos.water_bill(water_bill_id);

ALTER TABLE pos.water_payment
    ADD CONSTRAINT fk_water_payment_partner
        FOREIGN KEY (partner_id)
            REFERENCES pos.partner(partner_id);

ALTER TABLE pos.water_payment
    ADD CONSTRAINT fk_water_payment_type
        FOREIGN KEY (payment_type_id)
            REFERENCES pos.payment_type(payment_type_id);

ALTER TABLE pos.water_payment
    ADD CONSTRAINT fk_water_payment_cash_balance
        FOREIGN KEY (cash_balance_id)
            REFERENCES pos.cash_balance(cash_balance_id);

ALTER TABLE pos.water_payment
    ADD CONSTRAINT fk_water_payment_user
        FOREIGN KEY (user_id)
            REFERENCES pos.abac_user(user_id);

-- Step 11: Create indexes for optimization
CREATE INDEX idx_partner_water_connection ON pos.partner(water_connection_number);
CREATE INDEX idx_partner_water_meter ON pos.partner(water_meter_number);
CREATE INDEX idx_partner_connection_status ON pos.partner(connection_status_type_id);
CREATE INDEX idx_partner_debt ON pos.partner(current_debt) WHERE current_debt > 0;

CREATE INDEX idx_water_reading_partner ON pos.water_meter_reading(partner_id);
CREATE INDEX idx_water_reading_date ON pos.water_meter_reading(reading_date);
CREATE INDEX idx_water_reading_partner_date ON pos.water_meter_reading(partner_id, reading_date DESC);

CREATE INDEX idx_water_bill_partner ON pos.water_bill(partner_id);
CREATE INDEX idx_water_bill_status ON pos.water_bill(bill_status_type_id);
CREATE INDEX idx_water_bill_period ON pos.water_bill(billing_period_start, billing_period_end);
CREATE INDEX idx_water_bill_due_date ON pos.water_bill(due_date);
-- CREATE INDEX idx_water_bill_overdue ON pos.water_bill(due_date, bill_status_type_id)
--     WHERE active = true AND due_date < CURRENT_DATE;

CREATE INDEX idx_water_payment_partner ON pos.water_payment(partner_id);
CREATE INDEX idx_water_payment_bill ON pos.water_payment(water_bill_id);
CREATE INDEX idx_water_payment_date ON pos.water_payment(payment_date);

-- Step 12: Insert catalog data for connection_status_type
INSERT INTO pos.connection_status_type(connection_status_type_id, code, name, description, created_at, active)
VALUES
    (uuid_generate_v4(), 'ACTIVE', 'ACTIVA', 'Conexión de agua activa', CURRENT_TIMESTAMP, true),
    (uuid_generate_v4(), 'SUSPENDED', 'SUSPENDIDA', 'Conexión de agua suspendida temporalmente', CURRENT_TIMESTAMP, true),
    (uuid_generate_v4(), 'CUT_OFF', 'CORTADA', 'Conexión de agua cortada por falta de pago', CURRENT_TIMESTAMP, true),
    (uuid_generate_v4(), 'INACTIVE', 'INACTIVA', 'Conexión de agua inactiva', CURRENT_TIMESTAMP, true);

-- Step 13: Insert catalog data for bill_status_type
INSERT INTO pos.bill_status_type(bill_status_type_id, code, name, description, created_at, active)
VALUES
    (uuid_generate_v4(), 'PENDING', 'PENDIENTE', 'Factura pendiente de pago', CURRENT_TIMESTAMP, true),
    (uuid_generate_v4(), 'PARTIAL_PAID', 'PAGO PARCIAL', 'Factura con pago parcial', CURRENT_TIMESTAMP, true),
    (uuid_generate_v4(), 'PAID', 'PAGADA', 'Factura pagada completamente', CURRENT_TIMESTAMP, true),
    (uuid_generate_v4(), 'OVERDUE', 'VENCIDA', 'Factura vencida sin pagar', CURRENT_TIMESTAMP, true),
    (uuid_generate_v4(), 'CANCELLED', 'CANCELADA', 'Factura cancelada', CURRENT_TIMESTAMP, true);

-- Step 14: Update existing data - Set default connection status for existing partners
UPDATE pos.partner
SET connection_status_type_id = (
    SELECT connection_status_type_id
    FROM pos.connection_status_type
    WHERE code = 'ACTIVE'
    LIMIT 1
    )
WHERE water_connection_number IS NOT NULL;

-- Step 15: Add comments to tables and columns for documentation
COMMENT ON TABLE pos.partner IS 'Tabla de socios del sistema de cobranza de agua';
COMMENT ON COLUMN pos.partner.water_connection_number IS 'Número único de conexión de agua';
COMMENT ON COLUMN pos.partner.water_meter_number IS 'Número de medidor de agua';
COMMENT ON COLUMN pos.partner.current_debt IS 'Deuda actual acumulada del socio';
COMMENT ON COLUMN pos.partner.last_billing_date IS 'Fecha de última facturación';

COMMENT ON TABLE pos.water_meter_reading IS 'Registro de lecturas de medidores de agua';
COMMENT ON TABLE pos.water_bill IS 'Facturas de consumo de agua';
COMMENT ON TABLE pos.water_payment IS 'Pagos realizados sobre facturas de agua';
COMMENT ON TABLE pos.connection_status_type IS 'Catálogo de estados de conexión de agua';
COMMENT ON TABLE pos.bill_status_type IS 'Catálogo de estados de facturación';

-- =====================================================
-- END OF MIGRATION SCRIPT
-- =====================================================
-- To verify the migration, run:
-- SELECT * FROM pos.partner LIMIT 10;
-- SELECT * FROM pos.connection_status_type;
-- SELECT * FROM pos.bill_status_type;
-- =====================================================
