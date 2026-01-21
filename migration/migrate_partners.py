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

# IDs de estado de conexión proporcionados por el usuario
ID_STATUS_ACTIVE = '425a0052-b86d-47ed-af91-8a84cbfc7f65'
ID_STATUS_SUSPENDED = '8a5eb98d-4188-4974-a052-b31d954fc2df'

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
            # Handle short months
            if month == 1 and month_str[:3] in MONTHS_ES:
                month = MONTHS_ES[month_str[:3]]
            return datetime.date(year, month, day)
    except:
        pass
    return None

def migrate_partners_only():
    print("Iniciando migración de Socios con estados de conexión...")
    
    try:
        lite_conn = sqlite3.connect(SQLITE_DB_PATH)
        pg_conn = psycopg2.connect(**PG_CONFIG)
        lite_cur = lite_conn.cursor()
        pg_cur = pg_conn.cursor()

        # Limpiar datos previos
        print("Limpiando datos de socios previos...")
        pg_cur.execute("TRUNCATE TABLE partner CASCADE")

        # Obtenemos solo los socios que NO están eliminados
        lite_cur.execute("""
            SELECT CODIGO, NOMBRE, NUMERO_MEDIDOR, CI, DEUDA_ANTERIOR, 
                   SOCIO_ELIMINADO, FECHA_DELULTIMOPAGOREALIZADO, 
                   TERCERAEDAD, DEFUNCION, MANTENIMIENTO
            FROM Socio
            WHERE SOCIO_ELIMINADO = 0
        """)
        socios = lite_cur.fetchall()

        count_new = 0
        
        for row in socios:
            codigo, nombre, medidor, ci, deuda, eliminado, fecha_pago, es_tercera, defuncion, mantenimiento = row
            
            # Limpieza y preparación de datos
            active = True # Ya filtramos por SOCIO_ELIMINADO = 0
            is_elderly = (es_tercera == 1)
            last_billing = parse_spanish_date(fecha_pago)
            
            # Ajuste de estado de conexión según mantenimiento
            # Si mantenimiento es 1 (o '1'), el estado es SUSPENDIDO, de lo contrario ACTIVO
            connection_status_id = ID_STATUS_SUSPENDED if str(mantenimiento) == '1' else ID_STATUS_ACTIVE
            
            # Construcción de observaciones
            obs_parts = []
            if str(defuncion) == '1': obs_parts.append("Fallecido")
            if str(mantenimiento) == '1': obs_parts.append("En Mantenimiento")
            observation = " | ".join(obs_parts) if obs_parts else None

            # Insertar directamente (asumiendo tabla vacía)
            p_uuid = str(uuid.uuid4())
            pg_cur.execute(
                """INSERT INTO partner (
                    partner_id, partner_number, full_name, partner_identification_number, 
                    water_meter_number, current_debt, active, last_billing_date, 
                    is_elderly, observation, connection_status_type_id, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())""",
                (p_uuid, codigo, nombre, ci, medidor, deuda, active, last_billing, 
                 is_elderly, observation, connection_status_id)
            )
            count_new += 1

        pg_conn.commit()
        print(f"Migración completada:")
        print(f"  - Total socios registrados: {count_new}")

    except Exception as e:
        print(f"Error durante la migración: {e}")
        import traceback
        traceback.print_exc()
    finally:
        if lite_conn: lite_conn.close()
        if pg_conn: pg_conn.close()

if __name__ == "__main__":
    migrate_partners_only()
