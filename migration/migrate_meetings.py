import sqlite3
import psycopg2
import uuid
import datetime

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

# IDs de tipo de reunión obtenidos de Postgres
ID_TYPE_CLASSIC = '7d49e514-45b3-48ec-8fb8-87585d35e77a'
ID_TYPE_AULL = '632bb10a-0b00-4598-ad11-051910404a4a'

MONTHS_ES = {
    'ene': 1, 'feb': 2, 'mar': 3, 'abr': 4, 'may': 5, 'jun': 6,
    'jul': 7, 'ago': 8, 'sep': 9, 'oct': 10, 'nov': 11, 'dic': 12,
    'enero': 1, 'febrero': 2, 'marzo': 3, 'abril': 4, 'mayo': 5, 'junio': 6,
    'julio': 7, 'agosto': 8, 'septiembre': 9, 'octubre': 10, 'noviembre': 11, 'diciembre': 12
}

def parse_spanish_date(date_str):
    if not date_str or not isinstance(date_str, str) or date_str.lower() == 'null':
        return None
    try:
        date_str = date_str.lower().strip()
        parts = date_str.split('-')
        if len(parts) == 3: # dd-mon-yyyy
            day = int(parts[0])
            month_str = parts[1]
            year = int(parts[2])
            month = MONTHS_ES.get(month_str, 1)
            if month == 1 and month_str[:3] in MONTHS_ES:
                month = MONTHS_ES[month_str[:3]]
            return datetime.date(year, month, day)
    except:
        pass
    return None

def add_one_month(d):
    if not d: return d
    month = d.month + 1
    year = d.year
    if month > 12:
        month = 1
        year += 1
    # Mantener el mismo día, pero ajustarlo si el mes siguiente tiene menos días
    day = d.day
    while day > 28:
        try:
            return datetime.date(year, month, day)
        except ValueError:
            day -= 1
    return datetime.date(year, month, day)

def migrate_meetings():
    print("Iniciando migración de Reuniones (CLASSIC y AULL)...")
    
    lite_conn = sqlite3.connect(SQLITE_DB_PATH)
    pg_conn = psycopg2.connect(**PG_CONFIG)
    lite_cur = lite_conn.cursor()
    pg_cur = pg_conn.cursor()

    # Limpiar datos previos
    print("Limpiando datos de reuniones previas...")
    pg_cur.execute("TRUNCATE TABLE meeting_attendance CASCADE")
    pg_cur.execute("DELETE FROM meeting")

    # 1. Mapear socios
    pg_cur.execute("SELECT partner_id, partner_number FROM partner WHERE active = true")
    partner_map = {int(row[1]): row[0] for row in pg_cur.fetchall()}
    print(f"Socios mapeados: {len(partner_map)}")

    # 2. Pre-cargar multas específicas (MULTAID 2 para CLASSIC, 4 para AULL)
    print("Obteniendo montos de multas...")
    
    # Multas CLASSIC
    lite_cur.execute("SELECT IDDelMotivo, MAX(MONTO) FROM MultaAsignada WHERE MULTAID = 2 GROUP BY IDDelMotivo")
    classic_fines_map = {str(row[0]): row[1] for row in lite_cur.fetchall()}
    
    # Multas AULL
    lite_cur.execute("SELECT IDDelMotivo, MAX(MONTO) FROM MultaAsignada WHERE MULTAID = 4 GROUP BY IDDelMotivo")
    aull_fines_map = {str(row[0]): row[1] for row in lite_cur.fetchall()}

    # Multas por defecto
    lite_cur.execute("SELECT ID, MONTO FROM Multa WHERE ID IN (2, 4)")
    multas_defecto = {row[0]: row[1] for row in lite_cur.fetchall()}
    def_fine_classic = multas_defecto.get(2, 50.0)
    def_fine_aull = multas_defecto.get(4, 100.0)

    meeting_map = {} # (ID_SQLite, Type) -> (UUID_Postgres, Date)

    # 3. Migrar CLASSIC Meetings (Reunion)
    print("Migrando Reuniones CLASICAS...")
    lite_cur.execute("SELECT ID, ASUNTO, DESCRIPCION, HORA, MINUTO, PERIODO, FECHA FROM Reunion")
    for mid, asunto, desc, hora, minuto, periodo, fecha_str in lite_cur.fetchall():
        m_date = parse_spanish_date(fecha_str) or datetime.date.today()
        m_date = add_one_month(m_date)
        m_uuid = str(uuid.uuid4())
        meeting_map[(mid, 'CLASSIC')] = (m_uuid, m_date)
        
        fine = classic_fines_map.get(str(mid), def_fine_classic)
        
        try:
            h = int(hora) if hora and str(hora).isdigit() else 12
            m = int(minuto) if minuto and str(minuto).isdigit() else 0
            p = str(periodo).upper().replace('.', '')[:2] if periodo else 'AM'
        except:
            h, m, p = 12, 0, 'AM'

        pg_cur.execute(
            """INSERT INTO meeting (
                meeting_id, name, description, hour, minute, am_pm, meeting_date, 
                meeting_type_id, fine, active, created_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, True, NOW())""",
            (m_uuid, asunto, desc, h, m, p, m_date, ID_TYPE_CLASSIC, fine)
        )

    # 4. Migrar AULL Meetings
    print("Migrando Reuniones AULL...")
    lite_cur.execute("SELECT ID, NOMBRE, DESCRIPCION, FECHA FROM AULL")
    for aid, nombre, desc, fecha_str in lite_cur.fetchall():
        m_date = parse_spanish_date(fecha_str) or datetime.date.today()
        m_date = add_one_month(m_date)
        m_uuid = str(uuid.uuid4())
        meeting_map[(aid, 'AULL')] = (m_uuid, m_date)
        
        fine = aull_fines_map.get(str(aid), def_fine_aull)

        pg_cur.execute(
            """INSERT INTO meeting (
                meeting_id, name, description, hour, minute, am_pm, meeting_date, 
                meeting_type_id, fine, active, created_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, True, NOW())""",
            (m_uuid, nombre, desc, 8, 0, 'AM', m_date, ID_TYPE_AULL, fine)
        )

    # 5. Migrar Asistencias CLASSIC
    print("Migrando Asistencias CLASICAS...")
    lite_cur.execute("SELECT REUNIONID, SOCIOCODIGO, ASISTIO FROM Asistencia")
    rows_classic = lite_cur.fetchall()
    print(f"Total asistencias CLASICAS encontradas en SQLite: {len(rows_classic)}")
    
    att_classic_count = 0
    missing_meeting_classic = 0
    missing_partner_classic = 0
    
    for rid, scode, asistio in rows_classic:
        try:
            r_id = int(rid)
            s_id = scode if isinstance(scode, int) else int(scode)
            m_info = meeting_map.get((r_id, 'CLASSIC'))
            p_uuid = partner_map.get(s_id)
            
            if m_info and p_uuid:
                m_uuid, m_date = m_info
                pg_cur.execute(
                    """INSERT INTO meeting_attendance (
                        meeting_attendance_id, meeting_id, partner_id, attendance_date, present, active, created_at
                    ) VALUES (%s, %s, %s, %s, %s, True, NOW()) 
                    ON CONFLICT (meeting_id, partner_id, attendance_date) DO NOTHING""",
                    (str(uuid.uuid4()), m_uuid, p_uuid, m_date, asistio == 1)
                )
                att_classic_count += 1
            else:
                if not m_info: missing_meeting_classic += 1
                if not p_uuid: missing_partner_classic += 1
        except Exception as e:
            # print(f"Error en fila {rid}, {scode}: {e}")
            continue

    # 6. Migrar Asistencias AULL
    print("Migrando Asistencias AULL...")
    lite_cur.execute("SELECT AULLID, SOCIOCODIGO, ASISTIO FROM AULLAsignadoASocios")
    rows_aull = lite_cur.fetchall()
    print(f"Total asistencias AULL encontradas en SQLite: {len(rows_aull)}")
    
    att_aull_count = 0
    missing_meeting_aull = 0
    missing_partner_aull = 0
    
    for aid, scode, asistio in rows_aull:
        try:
            a_id = int(aid)
            s_id = scode if isinstance(scode, int) else int(scode)
            m_info = meeting_map.get((a_id, 'AULL'))
            p_uuid = partner_map.get(s_id)
            
            if m_info and p_uuid:
                m_uuid, m_date = m_info
                pg_cur.execute(
                    """INSERT INTO meeting_attendance (
                        meeting_attendance_id, meeting_id, partner_id, attendance_date, present, active, created_at
                    ) VALUES (%s, %s, %s, %s, %s, True, NOW())
                    ON CONFLICT (meeting_id, partner_id, attendance_date) DO NOTHING""",
                    (str(uuid.uuid4()), m_uuid, p_uuid, m_date, asistio == 1)
                )
                att_aull_count += 1
            else:
                if not m_info: missing_meeting_aull += 1
                if not p_uuid: missing_partner_aull += 1
        except Exception as e:
            # print(f"Error en fila {aid}, {scode}: {e}")
            continue

    pg_conn.commit()
    print("Plan de migración finalizado:")
    print(f"  - Asistencias CLASSIC insertadas: {att_classic_count}")
    print(f"    (Omitidas: {missing_meeting_classic} sin reunión, {missing_partner_classic} sin socio)")
    print(f"  - Asistencias AULL insertadas: {att_aull_count}")
    print(f"    (Omitidas: {missing_meeting_aull} sin reunión, {missing_partner_aull} sin socio)")
    
    lite_conn.close()
    pg_conn.close()

if __name__ == "__main__":
    migrate_meetings()
