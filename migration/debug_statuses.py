import psycopg2

PG_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'database': 'dreamsbo',
    'user': 'postgres',
    'password': 'admin008',
    'options': '-c search_path=pos'
}

def check_statuses():
    try:
        conn = psycopg2.connect(**PG_CONFIG)
        cur = conn.cursor()
        cur.execute("SELECT code, name FROM bill_status_type")
        rows = cur.fetchall()
        print("Estados encontrados en bill_status_type:")
        for code, name in rows:
            print(f" - {code}: {name}")
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    check_statuses()
