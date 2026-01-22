import psycopg2
import uuid
from decimal import Decimal

# --- CONFIGURACIÓN ---
PG_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'database': 'dreamsbo',
    'user': 'postgres',
    'password': 'admin008',
    'options': '-c search_path=pos'
}

# UUIDs de estados de factura
ID_STATUS_PENDING = '734d99f5-31ff-4d8f-8427-467dfcc5b76a'
ID_STATUS_PAID = 'b98eab78-6f41-4f93-af6f-10396d5d1f68'

def migrate_bill_concepts_postgres_direct():
    print("Iniciando migración de Conceptos (Jobs y Meetings) directamente desde Postgres...")
    
    try:
        pg_conn = psycopg2.connect(**PG_CONFIG)
        pg_cur = pg_conn.cursor()
    except Exception as e:
        print(f"Error de conexión: {e}")
        return

    # 1. Limpiar conceptos previos de trabajos y reuniones para evitar duplicados
    print("Limpiando conceptos de multas previos...")
    pg_cur.execute("""
        DELETE FROM bill_concept_item 
        WHERE concept_name LIKE 'Multa Trabajo:%' 
           OR concept_name LIKE 'Multa CLASICO:%' 
           OR concept_name LIKE 'Multa AULL:%'
    """)
    deleted_count = pg_cur.rowcount
    print(f"Conceptos previos eliminados: {deleted_count}")

    # 2. Obtener todas las facturas activas
    print("Cargando facturas de agua...")
    pg_cur.execute("""
        SELECT water_bill_id, partner_id, billing_period_start, total_amount, paid_amount, remaining_balance, bill_status_type_id 
        FROM water_bill 
        WHERE active = true
    """)
    bills = pg_cur.fetchall()
    print(f"Total facturas a procesar: {len(bills)}")

    concepts_inserted = 0
    bills_updated = 0
    
    # Procesar cada factura
    for b_id, p_id, bp_start, total, paid, remaining, status_id in bills:
        extra_amount = Decimal('0.00')
        concepts_to_add = []

        # Obtener el monto actual de los conceptos que QUEDARON en la factura tras el DELETE inicial
        pg_cur.execute("SELECT COALESCE(SUM(amount), 0) FROM bill_concept_item WHERE water_bill_id = %s", (b_id,))
        current_bill_total = pg_cur.fetchone()[0]

        # A. Buscar trabajos (jobs) no asistidos en el mismo mes/año
        pg_cur.execute("""
            SELECT j.name, j.fine 
            FROM job_attendance ja 
            JOIN job j ON ja.job_id = j.job_id 
            WHERE ja.partner_id = %s 
              AND ja.present = false 
              AND EXTRACT(MONTH FROM j.start_date) = %s 
              AND EXTRACT(YEAR FROM j.start_date) = %s
        """, (p_id, bp_start.month, bp_start.year))
        
        for name, fine in pg_cur.fetchall():
            concept_name = f"Multa Trabajo: {name.strip()}"
            concepts_to_add.append((concept_name, fine))
            extra_amount += fine

        # B. Buscar reuniones no asistidas en el mismo mes/año
        pg_cur.execute("""
            SELECT m.name, m.fine, mt.name as type_name
            FROM meeting_attendance ma 
            JOIN meeting m ON ma.meeting_id = m.meeting_id 
            JOIN meeting_type mt ON m.meeting_type_id = mt.meeting_type_id
            WHERE ma.partner_id = %s 
              AND ma.present = false 
              AND EXTRACT(MONTH FROM m.meeting_date) = %s 
              AND EXTRACT(YEAR FROM m.meeting_date) = %s
        """, (p_id, bp_start.month, bp_start.year))
        
        for name, fine, type_name in pg_cur.fetchall():
            concept_name = f"Multa {type_name}: {name.strip()}"
            concepts_to_add.append((concept_name, fine))
            extra_amount += fine

        # Insertar nuevos conceptos
        if concepts_to_add:
            for c_name, c_amount in concepts_to_add:
                pg_cur.execute("""
                    INSERT INTO bill_concept_item (
                        bill_concept_item_id, water_bill_id, concept_name, amount, assigned_date, active, created_at
                    ) VALUES (%s, %s, %s, %s, %s, True, NOW())
                """, (str(uuid.uuid4()), b_id, c_name, c_amount, bp_start))
                concepts_inserted += 1
        
        # SIEMPRE actualizar totales de la factura para asegurar sincronía, incluso si no añadimos nada nuevo
        # (porque el DELETE inicial pudo haber bajado el total)
        new_total = current_bill_total + extra_amount
        
        # Lógica de pago: 
        if status_id == ID_STATUS_PAID:
            new_paid = new_total
            new_remaining = Decimal('0.00')
        else:
            new_paid = paid
            new_remaining = new_total - paid # El saldo restante es el nuevo total menos lo que ya se pagó
        
        pg_cur.execute("""
            UPDATE water_bill 
            SET total_amount = %s, 
                paid_amount = %s, 
                remaining_balance = %s, 
                base_amount = %s 
            WHERE water_bill_id = %s
        """, (new_total, new_paid, new_remaining, new_total, b_id))
        
        bills_updated += 1

    pg_conn.commit()
    print("\nResumen de Migración Postgres-Directa:")
    print(f"  - Conceptos insertados: {concepts_inserted}")
    print(f"  - Facturas actualizadas con multas: {bills_updated}")

    # 3. Limpieza final: Facturas que quedaron en 0 (sin consumo y sin multas) -> PAGADAS
    print("\nFinalizando: Marcando facturas de 0 total como PAGADAS...")
    pg_cur.execute("""
        UPDATE water_bill 
        SET bill_status_type_id = %s,
            paid_date = COALESCE(paid_date, billing_period_end)
        WHERE total_amount = 0 
          AND remaining_balance = 0 
          AND bill_status_type_id = %s
          AND active = true
    """, (ID_STATUS_PAID, ID_STATUS_PENDING))
    zero_bills_fixed = pg_cur.rowcount
    print(f"  - Facturas de 0 total corregidas a PAGADAS: {zero_bills_fixed}")
    
    pg_conn.commit()
    pg_conn.close()

if __name__ == "__main__":
    migrate_bill_concepts_postgres_direct()
