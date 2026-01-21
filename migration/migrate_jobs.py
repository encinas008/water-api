import sqlite3
import psycopg2
from psycopg2 import extras
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

def get_pg_conn():
    return psycopg2.connect(**PG_CONFIG)

def get_lite_conn():
    return sqlite3.connect(SQLITE_DB_PATH)

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

def migrate_jobs():
    print("Iniciando migración de Trabajos (Jobs)...")
    
    lite_conn = get_lite_conn()
    pg_conn = get_pg_conn()
    lite_cur = lite_conn.cursor()
    pg_cur = pg_conn.cursor()

    # Limpiar datos previos incorrectos si existen
    print("Limpiando datos de trabajos previos...")
    pg_cur.execute("TRUNCATE TABLE job_attendance CASCADE")
    pg_cur.execute("DELETE FROM job")

    # Pre-cargar multas por trabajo para evitar consultas repetitivas en el loop
    print("Obteniendo montos de multas por trabajo...")
    lite_cur.execute("""
        SELECT IDDelMotivo, MAX(MONTO) 
        FROM MultaAsignada 
        WHERE MULTAID = 1 
        GROUP BY IDDelMotivo
    """)
    job_fines_map = {str(row[0]): row[1] for row in lite_cur.fetchall()}
    
    # Obtener multa por defecto de la tabla Multa
    lite_cur.execute("SELECT MONTO FROM Multa WHERE ID = 1")
    default_fine_res = lite_cur.fetchone()
    default_fine = default_fine_res[0] if default_fine_res else 100.0

    # 2. Migrar Trabajos
    lite_cur.execute("SELECT ID, NOMBRE, DESCRIPCION, FECHA FROM Trabajo")
    trabajos = lite_cur.fetchall()

    job_map = {} 
    for t_id, nombre, descripcion, fecha_str in trabajos:
        j_date = parse_spanish_date(fecha_str) or datetime.date.today()
        j_date = add_one_month(j_date)
        j_uuid = str(uuid.uuid4())
        job_map[t_id] = (j_uuid, j_date)

        # Usar la multa específica del trabajo o la por defecto
        fine_amount = job_fines_map.get(str(t_id), default_fine)

        pg_cur.execute(
            """INSERT INTO job (job_id, name, description, start_date, fine, active, created_at)
               VALUES (%s, %s, %s, %s, %s, True, NOW())""",
            (j_uuid, nombre, descripcion, j_date, fine_amount)
        )

    # 3. Migrar Asistencias
    # Recuperar mapeo de socios (partner_number -> partner_id)
    pg_cur.execute("SELECT partner_id, partner_number FROM partner WHERE active = true")
    partner_map = {int(row[1]): row[0] for row in pg_cur.fetchall()}
    print(f"Socios mapeados desde Postgres: {len(partner_map)}")

    lite_cur.execute("SELECT TRABAJOID, SOCIOCODIGO, ASISTIO FROM TrabajoAsignadoASocios")
    asistencias = lite_cur.fetchall()
    print(f"Asistencias encontradas en SQLite: {len(asistencias)}")

    attendance_inserted = 0
    missing_job = 0
    missing_partner = 0

    for job_id_lite, socio_codigo, asistio in asistencias:
        # Asegurar que los IDs sean enteros para el matching
        j_id = int(job_id_lite)
        s_id = int(socio_codigo)
        
        job_info = job_map.get(j_id)
        p_uuid = partner_map.get(s_id)

        if job_info and p_uuid:
            j_uuid, j_date = job_info
            pg_cur.execute(
                """INSERT INTO job_attendance (
                    attendance_id, job_id, partner_id, attendance_date, present, active, created_at
                ) VALUES (%s, %s, %s, %s, %s, True, NOW())""",
                (str(uuid.uuid4()), j_uuid, p_uuid, j_date, asistio == 1)
            )
            attendance_inserted += 1
        else:
            if not job_info: missing_job += 1
            if not p_uuid: missing_partner += 1

    pg_conn.commit()
    print(f"Migración de Trabajos y Asistencias completada.")
    print(f"  - Trabajos insertados: {len(job_map)}")
    print(f"  - Asistencias insertadas: {attendance_inserted}")
    print(f"  - Omitidas por Job faltante: {missing_job}")
    print(f"  - Omitidas por Socio faltante: {missing_partner}")
    lite_conn.close()
    pg_conn.close()

if __name__ == "__main__":
    migrate_jobs()
