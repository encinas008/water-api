import sqlite3
import psycopg2
import uuid
import datetime
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

SQLITE_DB_PATH = 'OTB.db'

# IDs proporcionados
ID_CASH_BALANCE = '87b4cebb-1de6-46e4-9761-cc306c0076da'
ID_PAYMENT_TYPE = 'acd30c4f-4a3a-468a-9330-a69d583ce445'
ID_USER = '03f10951-9d34-4424-9fc5-1a5b6ef252ae'

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
    parts = date_str.split('-')
    if len(parts) == 3:
        try:
            day = int(parts[0])
            month_str = parts[1].lower()[:3]
            year = int(parts[2])
            month_map = {'ene': 1, 'feb': 2, 'mar': 3, 'abr': 4, 'may': 5, 'jun': 6, 
                         'jul': 7, 'ago': 8, 'sep': 9, 'oct': 10, 'nov': 11, 'dic': 12}
            month = month_map.get(month_str, 1)
            return datetime.date(year, month, day)
        except: pass
    return None

def migrate_payments():
    print("Iniciando migración de Pagos (Water Payment)...")
    
    lite_conn = sqlite3.connect(SQLITE_DB_PATH)
    pg_conn = psycopg2.connect(**PG_CONFIG)
    lite_cur = lite_conn.cursor()
    pg_cur = pg_conn.cursor()

    # 1. Preparar esquema y Reiniciar correlativo
    print("Preparando esquema y reiniciando correlativo de caja...")
    pg_cur.execute("ALTER TABLE water_payment ALTER COLUMN water_bill_id DROP NOT NULL")
    pg_cur.execute(
        "UPDATE cash_balance SET last_correlative = 0 WHERE cash_balance_id = %s",
        (ID_CASH_BALANCE,)
    )
    
    # Limpiar pagos previos si existen (Opcional, pero recomendado para idempotencia)
    print("Limpiando pagos previos...")
    pg_cur.execute("TRUNCATE TABLE water_payment_detail CASCADE")
    pg_cur.execute("TRUNCATE TABLE water_payment CASCADE")

    # 2. Construir Mapas desde Postgres
    print("Construyendo mapas de entidades desde Postgres...")
    
    # Socios
    pg_cur.execute("SELECT partner_id, partner_number FROM partner WHERE active = true")
    partner_map = {int(row[1]): row[0] for row in pg_cur.fetchall()}
    
    # Facturas (usamos billing_period_start y partner_id)
    pg_cur.execute("SELECT water_bill_id, partner_id, billing_period_start FROM water_bill WHERE active = true")
    bill_map = {} # (partner_id, date) -> bill_id
    for bid, pid, bstart in pg_cur.fetchall():
        bill_map[(pid, bstart)] = bid

    # Mapear IDs de SQLite de Trabajos y Reuniones usando un truco:
    # Como no guardamos el ID de SQLite en Postgres, usaremos la fecha y el socio para las asistencias.
    
    # Asistencias de Trabajos
    pg_cur.execute("""
        SELECT ja.attendance_id, ja.partner_id, j.start_date, j.name 
        FROM job_attendance ja 
        JOIN job j ON ja.job_id = j.job_id
    """)
    job_att_map = {} # (partner_id, date) -> (attendance_id, name)
    for aid, pid, jdate, name in pg_cur.fetchall():
        job_att_map[(pid, jdate)] = (aid, name)

    # Asistencias de Reuniones
    pg_cur.execute("""
        SELECT ma.meeting_attendance_id, ma.partner_id, m.meeting_date, m.name 
        FROM meeting_attendance ma 
        JOIN meeting m ON ma.meeting_id = m.meeting_id
    """)
    meeting_att_map = {} # (partner_id, date) -> (attendance_id, name)
    for aid, pid, mdate, name in pg_cur.fetchall():
        meeting_att_map[(pid, mdate)] = (aid, name)

    # Pre-cargar IDs de estados para evitar consultas en el bucle
    print("Pre-cargando IDs de estados de factura...")
    pg_cur.execute("SELECT code, bill_status_type_id FROM bill_status_type WHERE active = true")
    status_id_map = {row[0]: row[1] for row in pg_cur.fetchall()}
    
    # Validar que los estados necesarios existen
    for code in ['PAID', 'PARTIAL_PAID', 'PENDING']:
        if code not in status_id_map:
            print(f"ERROR: No se encontró el estado {code} en bill_status_type")
            # Podríamos intentar insertarlos o lanzar error
            # Para mayor robustez, lanzamos error
            raise Exception(f"Falta el estado obligatorio '{code}' en la tabla bill_status_type.")

    # Mapear Lecturas para vincular pagos a facturas
    # (socio_id, mes_cobro) -> mes_correspondiente (que es el billing_period_start)
    print("Construyendo mapa de periodos desde LecturaDeAgua...")
    lite_cur.execute("SELECT CODIGOSOCIO, mesDeCobro, mesCorrespondiente FROM LecturaDeAgua")
    period_map = {}
    for scode, mcobro, mcorr in lite_cur.fetchall():
        try:
            period_map[(int(float(scode)), mcobro)] = mcorr
        except: continue

    # 3. Pull paid multas from SQLite
    print("Obteniendo multas pagadas de SQLite...")
    lite_cur.execute("""
        SELECT SOCIOCODIGO, FECHACOMPLETA, MULTAID, MONTO, FECHA, MESYANIOQUESECOBRARA 
        FROM MultaAsignada 
        WHERE COBRADO = 1 AND FECHACOMPLETA IS NOT NULL AND FECHACOMPLETA != '""'
    """)
    rows = lite_cur.fetchall()
    
    # Agrupar por (socio, fecha_pago)
    payment_groups = {}
    for scode, fecha_p, mid, monto, fecha_orig, mcobro in rows:
        try:
            k = (int(scode), fecha_p)
            if k not in payment_groups: payment_groups[k] = []
            payment_groups[k].append({
                'mid': mid, 'monto': monto, 'fecha_orig': fecha_orig, 'mcobro': mcobro
            })
        except: continue

    print(f"Total grupos de pago encontrados: {len(payment_groups)}")

    correlative = 0
    payments_inserted = 0
    details_inserted = 0

    # 4. Procesar Grupos
    for (s_id, fecha_p_str), components in payment_groups.items():
        p_uuid = partner_map.get(s_id)
        if not p_uuid: continue
        
        pay_date = parse_lite_date(fecha_p_str)
        if not pay_date: continue

        # Deduplicar componentes
        unique_components = []
        seen_components = set()
        for c in components:
            comp_key = (c['mid'], c['fecha_orig'], str(c['monto']), c['mcobro'])
            if comp_key not in seen_components:
                unique_components.append(c)
                seen_components.add(comp_key)
        
        # Agrupar componentes por periodo de factura
        by_period = {} # date -> list of components
        standalone = []
        
        for c in unique_components:
            period = None
            if c['mid'] in ('11', '12', '13', '5'):
                m_corr_str = period_map.get((s_id, c['mcobro']))
                if m_corr_str:
                    period = parse_month_year(m_corr_str)
            
            if period:
                if period not in by_period: by_period[period] = []
                by_period[period].append(c)
            else:
                standalone.append(c)
        
        # Generar pagos. Si no hay periodos, todo va a un pago sin factura.
        # Si hay periodos, los standalone se asocian al primer periodo.
        payment_tasks = []
        if not by_period:
            payment_tasks.append({'bill_period': None, 'components': standalone})
        else:
            periods = sorted(by_period.keys())
            for i, period in enumerate(periods):
                comps = by_period[period]
                if i == 0: comps += standalone # Fines y otros van en el primero
                payment_tasks.append({'bill_period': period, 'components': comps})

        for i, task in enumerate(payment_tasks):
            b_period = task['bill_period']
            t_components = task['components']
            total_amount = sum(c['monto'] for c in t_components)
            
            bill_uuid = bill_map.get((p_uuid, b_period)) if b_period else None
            
            correlative += 1
            payment_uuid = str(uuid.uuid4())
            
            # Suffix para el recibo si hay múltiples pagos para la misma fecha/socio
            suffix = f"-{i+1}" if len(payment_tasks) > 1 else ""
            receipt_number = f"RC-{s_id}-{int(pay_date.strftime('%s')) % 100000}-{correlative}{suffix}"
            
            # Insertar Water Payment
            pg_cur.execute(
                """INSERT INTO water_payment (
                    water_payment_id, water_bill_id, partner_id, payment_date, 
                    amount, payment_type_id, cash_balance_id, user_id, 
                    receipt_number, correlative_number, active, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, True, NOW())""",
                (payment_uuid, bill_uuid, p_uuid, pay_date, total_amount, 
                 ID_PAYMENT_TYPE, ID_CASH_BALANCE, ID_USER, receipt_number, correlative)
            )
            payments_inserted += 1

            # --- NUEVA LÓGICA: Actualizar la Factura y Deuda del Socio ---
            if bill_uuid:
                # 1. Obtener datos actuales de la factura
                pg_cur.execute(
                    "SELECT total_amount, paid_amount, remaining_balance FROM water_bill WHERE water_bill_id = %s",
                    (bill_uuid,)
                )
                bill_data = pg_cur.fetchone()
                if bill_data:
                    b_total, b_paid, b_rem = bill_data
                    
                    # Asegurar tipos Decimal y manejar NULLs
                    b_total = Decimal(str(b_total)) if b_total is not None else Decimal('0.00')
                    b_paid = Decimal(str(b_paid)) if b_paid is not None else Decimal('0.00')
                    current_total_amount = Decimal(str(total_amount))
                    
                    new_paid = b_paid + current_total_amount
                    new_rem = max(Decimal('0.00'), b_total - new_paid)
                    
                    # 2. Determinar nuevo estado
                    st_code = 'PAID' if new_rem <= 0 else 'PARTIAL_PAID'
                    # Fallback si por alguna razón no existe PARTIAL_PAID en la DB
                    new_status_id = status_id_map.get(st_code, status_id_map.get('PENDING'))
                    
                    pg_cur.execute(
                        """UPDATE water_bill 
                           SET paid_amount = %s, remaining_balance = %s, bill_status_type_id = %s, paid_date = %s
                           WHERE water_bill_id = %s""",
                        (new_paid, new_rem, new_status_id, pay_date if new_rem <= 0 else None, bill_uuid)
                    )
                
                # 3. Actualizar deuda del socio (restar el pago)
                pg_cur.execute(
                    "UPDATE partner SET current_debt = COALESCE(current_debt, 0) - %s WHERE partner_id = %s",
                    (current_total_amount, p_uuid)
                )

            # Insertar Detalles para Multas (0, 1, 2, 4)
            for c in t_components:
                fine_type = None
                if c['mid'] == '1': fine_type = 'JOB'
                elif c['mid'] in ('0', '2', '4'): fine_type = 'MEETING'
                
                if fine_type:
                    d_orig = parse_lite_date(c['fecha_orig'])
                    if not d_orig: continue
                    
                    att_info = job_att_map.get((p_uuid, d_orig)) if fine_type == 'JOB' else meeting_att_map.get((p_uuid, d_orig))
                    
                    if att_info:
                        att_id, att_name = att_info
                        pg_cur.execute(
                            """INSERT INTO water_payment_detail (
                                water_payment_detail_id, water_payment_id, fine_type, 
                                fine_id, fine_name, fine_date, fine_amount, active, created_at
                            ) VALUES (%s, %s, %s, %s, %s, %s, %s, True, NOW())
                            ON CONFLICT (water_payment_id, fine_type, fine_id) DO NOTHING""",
                            (str(uuid.uuid4()), payment_uuid, fine_type, att_id, att_name, d_orig, c['monto'])
                        )
                        details_inserted += 1

    # Actualizar last_correlative al final
    pg_cur.execute(
        "UPDATE cash_balance SET last_correlative = %s WHERE cash_balance_id = %s",
        (correlative, ID_CASH_BALANCE)
    )

    pg_conn.commit()
    print("Migración de Pagos completada:")
    print(f"  - Pagos insertados: {payments_inserted}")
    print(f"  - Detalles (multas) insertados: {details_inserted}")
    
    lite_conn.close()
    pg_conn.close()

if __name__ == "__main__":
    migrate_payments()
