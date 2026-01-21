import sqlite3
import psycopg2
import uuid
import datetime
import re

# --- CONFIGURACIÓN ---
PG_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'database': 'dreamsbo',
    'user': 'postgres',
    'password': 'admin008',
    'options': '-c search_path=pos'
}

SQLITE_DB_PATH = 'OTB.db'

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

    # 1. Agrupar SOLO conceptos de AGUA (11, 12, 13, 5) para evitar doble cobro con las tablas de asistencia
    print("Pre-procesando conceptos de AGUA desde SQLite...")
    lite_cur.execute("""
        SELECT SOCIOCODIGO, MESYANIOQUESECOBRARA, MULTAID, MONTO, COBRADO, FECHACOMPLETA 
        FROM MultaAsignada
        WHERE MULTAID IN ('5', '11', '12', '13')
    """)
    multas_grouped = {} # (socio_id, mes_cobro) -> list of components
    for scode, mes_cobro, mid, monto, cobrado, fecha_p in lite_cur.fetchall():
        try:
            k = (int(float(scode)), mes_cobro)
            if k not in multas_grouped: multas_grouped[k] = []
            multas_grouped[k].append({
                'id': mid, 'amount': monto, 'paid': cobrado == 1, 'pay_date': fecha_p
            })
        except: continue

    concept_names_map = {
        '11': 'Cargo Fijo / Formulario',
        '12': 'Consumo Mínimo',
        '13': 'Alcantarillado / Mantenimiento',
        '5': 'Exceso de Consumo de Agua'
    }

    # 2. Procesar lecturas y crear facturas únicas por mes de cobro
    print("Migrando Lecturas y Facturas...")
    lite_cur.execute("SELECT CODIGOSOCIO, mesCorrespondiente, mesDeCobro, lecturaRegistrada FROM LecturaDeAgua")
    lecturas = lite_cur.fetchall()
    
    # Ordenar por socio y mes Correspondiente para calcular bien el consumo
    def sort_key_corr(x):
        try:
            d = parse_month_year(x[1]) 
            return (int(float(x[0])), d if d else datetime.date(1900, 1, 1))
        except: return (0, datetime.date(1900, 1, 1))
    
    lecturas_sorted = sorted(lecturas, key=sort_key_corr)

    processed_bills = set() # (socio_id, mes_cobro) para evitar duplicados
    last_readings = {} 
    reading_count = 0
    bill_count = 0

    for scode, mes_corr, mes_cobro, val_str in lecturas_sorted:
        try:
            s_id = int(float(scode))
            p_uuid = partner_map.get(s_id)
            if not p_uuid: continue

            # Clave única para evitar duplicar facturas del mismo mes de cobro
            bill_key = (s_id, mes_cobro)
            if bill_key in processed_bills: continue

            val = round(float(val_str), 2)
            prev_val = last_readings.get(s_id, 0.0)
            consumption = round(max(0, val - prev_val), 2)
            last_readings[s_id] = val

            # La factura se etiqueta con el Mes de Cobro (ej. "Septiembre-2025")
            r_date_invoice = parse_month_year(mes_cobro)
            if not r_date_invoice: continue
            
            # La fecha de la lectura física es el mes Correspondiente
            r_date_reading = parse_month_year(mes_corr)
            r_date_reading_end = get_billing_period_end(r_date_reading) if r_date_reading else r_date_invoice
            
            r_uuid = str(uuid.uuid4())
            pg_cur.execute(
                """INSERT INTO water_meter_reading (
                    reading_id, partner_id, reading_date, current_reading, 
                    previous_reading, consumption, active, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, True, NOW())""",
                (r_uuid, p_uuid, r_date_reading_end, val, prev_val, consumption)
            )
            reading_count += 1

            components = multas_grouped.get(bill_key, [])
            total_amount = sum(c['amount'] for c in components) if components else 0.0
            
            is_paid = all(c['paid'] for c in components) if components else False
            pay_date_str = next((c['pay_date'] for c in reversed(components) if c['pay_date'] and c['pay_date'] != '""'), None)
            pay_date = parse_lite_date(pay_date_str)
            
            status_id = ID_STATUS_PAID if (is_paid or total_amount <= 0) else ID_STATUS_PENDING
            remaining = 0.0 if (is_paid or total_amount <= 0) else total_amount
            paid_amt = total_amount if (is_paid or total_amount <= 0) else 0.0

            b_uuid = str(uuid.uuid4())
            bill_number = f"FAC-{s_id}-{reading_count}"

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
            processed_bills.add(bill_key)

            for c in components:
                name = concept_names_map.get(c['id'], "Concepto Agua")
                pg_cur.execute(
                    """INSERT INTO bill_concept_item (
                        bill_concept_item_id, water_bill_id, concept_name, amount, assigned_date, active, created_at
                    ) VALUES (%s, %s, %s, %s, %s, True, NOW())""",
                    (str(uuid.uuid4()), b_uuid, name, c['amount'], r_date_invoice)
                )

        except Exception as e:
            print(f"Error procesando socio {scode} en periodo {mes_cobro}: {e}")
            continue

        except Exception as e:
            print(f"Error procesando socio {scode} en periodo {mes_cobro}: {e}")
            continue

        except Exception as e:
            # No hacemos rollback total aquí porque matamos la transacción para el resto de filas
            # Simplemente imprimimos el error y seguimos
            print(f"Error procesando lectura {scode} {mes_corr}: {e}")
            continue

    pg_conn.commit()
    print("Migración de Consumos finalizada:")
    print(f"  - Lecturas insertadas: {reading_count}")
    print(f"  - Facturas generadas: {bill_count}")
    
    lite_conn.close()
    pg_conn.close()

if __name__ == "__main__":
    migrate_readings_and_bills()
