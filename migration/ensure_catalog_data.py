import psycopg2
import uuid

# --- CONFIGURACIÓN ---
PG_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'database': 'dreamsbo',
    'user': 'postgres',
    'password': 'admin008',
    'options': '-c search_path=pos'
}

def ensure_partial_paid_status():
    print("Verificando existencia del estado PARTIAL_PAID...")
    
    try:
        conn = psycopg2.connect(**PG_CONFIG)
        cur = conn.cursor()
        
        # Verificar si ya existe
        cur.execute("SELECT bill_status_type_id FROM bill_status_type WHERE code = 'PARTIAL_PAID'")
        row = cur.fetchone()
        
        if row:
            print(f"El estado PARTIAL_PAID ya existe (ID: {row[0]})")
        else:
            print("Estado PARTIAL_PAID no encontrado. Creando...")
            cur.execute("""
                INSERT INTO bill_status_type (bill_status_type_id, code, name, description, active, created_at)
                VALUES (%s, %s, %s, %s, %s, NOW())
            """, (str(uuid.uuid4()), 'PARTIAL_PAID', 'PAGO PARCIAL', 'Factura de Pago Parcial', True))
            conn.commit()
            print("Estado PARTIAL_PAID creado con éxito.")
            
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error al verificar/crear estado: {e}")
        exit(1)

if __name__ == "__main__":
    ensure_partial_paid_status()
