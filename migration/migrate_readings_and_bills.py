import sqlite3
import psycopg2
import uuid
import datetime
import re
import os

# --- CONFIGURACIÓN ---
PG_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'database': 'dreamsbo',
    'user': 'postgres',
    'password': 'admin008',
    'options': '-c search_path=pos'
}

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SQLITE_DB_PATH = os.path.normpath(os.path.join(BASE_DIR, '..', 'OTB.db'))

# UUIDs de estados de factura
ID_STATUS_PENDING = '734d99f5-31ff-4d8f-8427-467dfcc5b76a'
ID_STATUS_PAID = 'b98eab78-6f41-4f93-af6f-10396d5d1f68'

MONTHS_ES = {
    'enero': 1, 'febrero': 2, 'marzo': 3, 'abril': 4, 'mayo': 5, 'junio': 6,
    'julio': 7, 'agosto': 8, 'septiembre': 9, 'octubre': 10, 'noviembre': 11, 'diciembre': 12
}

def parse_month_year(s):
    if not s: return None
    parts = s.split('-')
    if len(parts) == 2:
        month_name = parts[0].lower().strip()
        month = MONTHS_ES.get(month_name)
        year = int(parts[1])
        if month:
            return datetime.date(year, month, 1)
    return None

def parse_lite_date(date_str):
    if not date_str or date_str == '""' or date_str == 'None': return None
    # Formato: 18-marzo-2018 o 01-ene-2018
    parts = date_str.split('-')
    if len(parts) == 3:
        day = int(parts[0])
        month_str = parts[1].lower()[:3]
        year = int(parts[2])
        month_map = {'ene': 1, 'feb': 2, 'mar': 3, 'abr': 4, 'may': 5, 'jun': 6, 
                     'jul': 7, 'ago': 8, 'sep': 9, 'oct': 10, 'nov': 11, 'dic': 12,
                     'ene': 1, 'fev': 2, 'mar': 3, 'abr': 4, 'mai': 5, 'jun': 6,
                     'jul': 7, 'ago': 8, 'set': 9, 'out': 10, 'nov': 11, 'dez': 12}
                     # Adding some variations just in case
        month = month_map.get(month_str, 1)
        # Check full names if 3 letters didn't work
        full_month_map = {k[:3]: v for k, v in MONTHS_ES.items()}
        month = full_month_map.get(month_str, month)
        
        return datetime.date(year, month, day)
    return None

def get_billing_period_end(start_date):
    if not start_date: return None
    # El fin de mes de start_date
    next_month = start_date.replace(day=28) + datetime.timedelta(days=4)
    return next_month - datetime.timedelta(days=next_month.day)

def migrate_readings_and_bills():
    print("Iniciando migración de Lecturas y Facturas...")
    
    lite_conn = sqlite3.connect(SQLITE_DB_PATH)
    pg_conn = psycopg2.connect(**PG_CONFIG)
    lite_cur = lite_conn.cursor()
    pg_cur = pg_conn.cursor()

    # Limpiar datos previos
    print("Limpiando datos previos en Postgres...")
    pg_cur.execute("TRUNCATE TABLE bill_concept_item CASCADE")
    pg_cur.execute("TRUNCATE TABLE water_bill CASCADE")
    pg_cur.execute("TRUNCATE TABLE water_meter_reading CASCADE")

    # Mapeo de socios
    pg_cur.execute("SELECT partner_id, partner_number FROM partner WHERE active = true")
    partner_map = {int(row[1]): row[0] for row in pg_cur.fetchall()}
    print(f"Socios mapeados: {len(partner_map)}")

    # Obtener nombres de conceptos (opcional para el detalle)
    concept_names = {
        '11': 'Cargo Fijo / Formulario',
        '12': 'Consumo Mínimo',
        '13': 'Alcantarillado / Mantenimiento',
        '5': 'Exceso de Consumo de Agua'
    }

    # 1. Agrupar conceptos de la tabla MultaAsignada (Excluyendo Trabajos y Reuniones)
    print("Pre-procesando conceptos desde SQLite...")
    lite_cur.execute("""
        SELECT SOCIOCODIGO, MESYANIO, MULTAID, MONTO, COBRADO, FECHACOMPLETA, IDDelMotivo 
        FROM MultaAsignada
        WHERE MULTAID NOT IN ('1', '2', '0', '4')
    """)
    multas_grouped = {} # (socio_id, mes_ocurrencia) -> list of components
    for scode, mes_ocurr, mid, monto, cobrado, fecha_p, motive_id in lite_cur.fetchall():
        try:
            if not mes_ocurr: continue
            k = (int(float(scode)), mes_ocurr)
            if k not in multas_grouped: multas_grouped[k] = []
            multas_grouped[k].append({
                'id': mid, 'amount': monto, 'paid': cobrado != 0, 'pay_date': fecha_p, 'motive_id': motive_id
            })
        except: continue

    concept_names_map = {
        '11': 'Cargo Fijo / Formulario',
        '12': 'Consumo Mínimo',
        '13': 'Alcantarillado / Mantenimiento',
        '5': 'Exceso de Consumo de Agua',
        '3': 'Multa por mora',
        '6': 'Multa aporte cordones'
    }

    def get_rich_name(mid, motive_id):
        base_name = concept_names_map.get(str(mid), f"Concepto {mid}")
        if not motive_id or motive_id == 'null' or motive_id == '':
            return base_name
        
        try:
            if str(mid) == '4': # AULL
                lite_cur.execute("SELECT NOMBRE FROM AULL WHERE ID = ?", (motive_id,))
                res = lite_cur.fetchone()
                if res: return f"Multa AULL: {res[0]}"
        except: pass
        return base_name

    # 2. Procesar lecturas y multas para crear facturas únicas por mes de ocurrencia
    print("Migrando Lecturas y Facturas...")
    
    # Obtener todas las lecturas (mesCorrespondiente es el mes de consumo)
    lite_cur.execute("SELECT CODIGOSOCIO, mesCorrespondiente, lecturaRegistrada FROM LecturaDeAgua")
    readings_map = {} # (s_id, mes_ocurr) -> val_str
    all_bill_keys = set()
    
    for scode, mes_corr, val_str in lite_cur.fetchall():
        try:
            if not mes_corr: continue
            s_id = int(float(scode))
            all_bill_keys.add((s_id, mes_corr))
            readings_map[(s_id, mes_corr)] = val_str
        except: continue
        
    for k in multas_grouped.keys():
        all_bill_keys.add(k)
        
    # Ordenar llaves por socio y luego por fecha
    def sort_key_bills(k):
        s_id, mes_ocurr = k
        d = parse_month_year(mes_ocurr)
        return (s_id, d if d else datetime.date(1900, 1, 1))
    
    sorted_keys = sorted(list(all_bill_keys), key=sort_key_bills)

    last_readings = {} 
    reading_count = 0
    bill_count = 0

    for s_id, mes_ocurr in sorted_keys:
        try:
            p_uuid = partner_map.get(s_id)
            if not p_uuid: continue

            # La factura se etiqueta con el Mes de Ocurrencia (ej. "Septiembre-2025")
            r_date_invoice = parse_month_year(mes_ocurr)
            if not r_date_invoice: continue
            
            # Datos de lectura si existen
            val_str = readings_map.get((s_id, mes_ocurr))
            r_uuid = None
            consumption = 0.0
            prev_val = last_readings.get(s_id, 0.0)
            val = prev_val

            if val_str is not None:
                val = round(float(val_str), 2)
                consumption = round(max(0, val - prev_val), 2)
                last_readings[s_id] = val
                
                # La fecha de la lectura física
                r_date_reading_end = get_billing_period_end(r_date_invoice)
                
                r_uuid = str(uuid.uuid4())
                pg_cur.execute(
                    """INSERT INTO water_meter_reading (
                        reading_id, partner_id, reading_date, current_reading, 
                        previous_reading, consumption, active, created_at
                    ) VALUES (%s, %s, %s, %s, %s, %s, True, NOW())""",
                    (r_uuid, p_uuid, r_date_reading_end, val, prev_val, consumption)
                )
                reading_count += 1

            components = multas_grouped.get((s_id, mes_ocurr), [])
            total_amount = sum(c['amount'] for c in components) if components else 0.0
            
            # Un bill está PAGADO si TODOS sus componentes están marcados como COBRADO=1
            is_paid = all(c['paid'] for c in components) if components else True
            # Si no hay componentes pero hubo lectura con consumo 0, total es 0 y es PAID
            if not components and consumption == 0:
                is_paid = True
            
            pay_date_str = next((c['pay_date'] for c in reversed(components) if c['pay_date'] and c['pay_date'] != '""'), None)
            pay_date = parse_lite_date(pay_date_str)
            
            status_id = ID_STATUS_PAID if (is_paid or total_amount <= 0) else ID_STATUS_PENDING
            remaining = 0.0 if (is_paid or total_amount <= 0) else total_amount
            paid_amt = total_amount if (is_paid or total_amount <= 0) else 0.0

            b_uuid = str(uuid.uuid4())
            bill_number = f"FAC-{s_id}-{28000 + bill_count + 1}" # Usamos un correlativo alto para evitar conflictos o seguir el anterior

            pg_cur.execute(
                """INSERT INTO water_bill (
                    water_bill_id, bill_number, partner_id, reading_id, 
                    billing_period_start, billing_period_end, consumption_m3, 
                    rate_per_m3, base_amount, total_amount, paid_amount, 
                    remaining_balance, bill_status_type_id, due_date, 
                    paid_date, active, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, True, NOW())""",
                (b_uuid, bill_number, p_uuid, r_uuid, r_date_invoice, get_billing_period_end(r_date_invoice), 
                 consumption, 0, total_amount, total_amount, paid_amt, 
                 remaining, status_id, r_date_invoice + datetime.timedelta(days=15), pay_date)
            )
            bill_count += 1

            for c in components:
                name = get_rich_name(c['id'], c['motive_id'])
                pg_cur.execute(
                    """INSERT INTO bill_concept_item (
                        bill_concept_item_id, water_bill_id, concept_name, amount, assigned_date, active, created_at
                    ) VALUES (%s, %s, %s, %s, %s, True, NOW())""",
                    (str(uuid.uuid4()), b_uuid, name, c['amount'], r_date_invoice)
                )

        except Exception as e:
            print(f"Error procesando socio {s_id} en periodo {mes_ocurr}: {e}")
            continue

        except Exception as e:
            print(f"Error procesando socio {s_id} en periodo {mes_cobro}: {e}")
            continue

        except Exception as e:
            # No hacemos rollback total aquí porque matamos la transacción para el resto de filas
            # Simplemente imprimimos el error y seguimos
            print(f"Error procesando lectura {s_id} {mes_cobro}: {e}")
            continue

    pg_conn.commit()
    print("Migración de Consumos finalizada:")
    print(f"  - Lecturas insertadas: {reading_count}")
    print(f"  - Facturas generadas: {bill_count}")
    
    lite_conn.close()
    pg_conn.close()

if __name__ == "__main__":
    migrate_readings_and_bills()
