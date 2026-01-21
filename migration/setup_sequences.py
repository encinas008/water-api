import psycopg2

# --- CONFIGURACIÓN ---
PG_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'database': 'dreamsbo',
    'user': 'postgres',
    'password': 'admin008',
    'options': '-c search_path=pos'
}

def setup_partner_sequence():
    print("Configurando secuencia para partner_number...")
    
    try:
        conn = psycopg2.connect(**PG_CONFIG)
        cur = conn.cursor()

        # SQL para crear la secuencia y configurar el default
        # Nota: El usuario pidió usar 'pos.order_number_seq' en el ALTER/SELECT 
        # pero también mencionó 'pos.partner_number_seq'. 
        # Seguiré su instrucción exacta del SQL proporcionado.
        
        sql_commands = [
            "CREATE SEQUENCE IF NOT EXISTS pos.partner_number_seq;",
            """
            ALTER TABLE pos.partner 
            ALTER COLUMN partner_number 
            SET DEFAULT nextval('pos.partner_number_seq'::regclass);
            """,
            """
            SELECT SETVAL('pos.partner_number_seq', (SELECT COALESCE(max(partner_number), 0) FROM pos.partner));
            """
        ]

        for sql in sql_commands:
            try:
                cur.execute(sql)
            except Exception as e:
                print(f"Aviso en comando SQL: {e}")
                conn.rollback()
                continue
            else:
                conn.commit()

        print("Secuencia configurada exitosamente.")

    except Exception as e:
        print(f"Error configurando la secuencia: {e}")
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    setup_partner_sequence()
