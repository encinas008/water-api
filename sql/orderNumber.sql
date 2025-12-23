CREATE SEQUENCE IF NOT EXISTS pos.order_number_seq;

ALTER TABLE pos.sale
    ALTER COLUMN order_number
        SET DEFAULT nextval('pos.order_number_seq'::regclass);

SELECT SETVAL('pos.order_number_seq', (SELECT max(order_number)
                                       FROM pos.sale));
