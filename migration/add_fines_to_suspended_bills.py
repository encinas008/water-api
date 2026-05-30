#!/usr/bin/env python3
"""
Script para migrar multas a conceptos en facturas de socios SUSPENDIDOS
Busca todas las facturas de socios suspendidos que no tienen las multas agregadas como conceptos
y las agrega automáticamente, actualizando el total de la factura.
"""
import psycopg2
import uuid
from decimal import Decimal
from datetime import datetime

# --- CONFIGURACIÓN ---
PG_CONFIG = {
    'host': 'localhost',
    'port': '5432',
    'database': 'dreamsbo',
    'user': 'postgres',
    'password': 'admin008',
    'options': '-c search_path=pos'
}

def connect_db():
    """Conectar a la base de datos"""
    try:
        conn = psycopg2.connect(**PG_CONFIG)
        return conn
    except Exception as e:
        print(f"❌ Error de conexión: {e}")
        return None

def migrate_suspended_partner_bills():
    """Migra multas a conceptos para facturas de socios suspendidos"""
    print("=" * 80)
    print("MIGRACIÓN: Agregar multas a facturas de socios SUSPENDIDOS")
    print("=" * 80)
    
    conn = connect_db()
    if not conn:
        return
    
    try:
        cur = conn.cursor()
        
        # 1. Obtener todas las facturas de socios SUSPENDIDOS
        print("\n[1] Buscando facturas de socios SUSPENDIDOS...")
        cur.execute("""
            SELECT 
                wb.water_bill_id,
                wb.partner_id,
                p.full_name,
                p.partner_number,
                wb.billing_period_start,
                wb.total_amount,
                wb.bill_status_type_id
            FROM water_bill wb
            JOIN partner p ON wb.partner_id = p.partner_id
            JOIN connection_status_type cst ON p.connection_status_type_id = cst.connection_status_type_id
            WHERE wb.active = true
              AND cst.code = 'SUSPENDED'
            ORDER BY wb.billing_period_start DESC, p.partner_number
        """)
        
        suspended_bills = cur.fetchall()
        print(f"   ✓ Encontradas {len(suspended_bills)} facturas de socios suspendidos")
        
        if not suspended_bills:
            print("   ℹ️  No hay facturas de socios suspendidos")
            return
        
        # 2. Procesar cada factura
        total_fines_added = 0
        bills_updated = 0
        
        for bill_id, partner_id, partner_name, partner_number, period_start, total_amount, status_id in suspended_bills:
            print(f"\n   Procesando: #{partner_number} {partner_name} ({period_start.strftime('%B %Y')})")
            
            # Obtener multas pendientes de trabajos y reuniones
            job_fines = []
            meeting_fines = []
            
            # 2.1. Buscar multas de trabajos (ausencias)
            cur.execute("""
                SELECT j.job_id, j.name, j.fine, j.start_date
                FROM job_attendance ja
                JOIN job j ON ja.job_id = j.job_id
                WHERE ja.partner_id = %s
                  AND ja.present = false
                  AND EXTRACT(MONTH FROM j.start_date) = %s
                  AND EXTRACT(YEAR FROM j.start_date) = %s
                  AND j.active = true
            """, (partner_id, period_start.month, period_start.year))
            
            job_fines = cur.fetchall()
            
            # 2.2. Buscar multas de reuniones (ausencias)
            cur.execute("""
                SELECT m.meeting_id, m.name, m.fine, m.meeting_date
                FROM meeting_attendance ma
                JOIN meeting m ON ma.meeting_id = m.meeting_id
                WHERE ma.partner_id = %s
                  AND ma.present = false
                  AND EXTRACT(MONTH FROM m.meeting_date) = %s
                  AND EXTRACT(YEAR FROM m.meeting_date) = %s
                  AND m.active = true
            """, (partner_id, period_start.month, period_start.year))
            
            meeting_fines = cur.fetchall()
            
            if not job_fines and not meeting_fines:
                print("      → Sin multas pendientes")
                continue
            
            print(f"      → Encontradas {len(job_fines)} multas de trabajos + {len(meeting_fines)} de reuniones")
            
            # 2.3. Verificar si ya existen estos conceptos en la factura
            concepts_to_add = []
            
            for job_id, job_name, job_fine, job_date in job_fines:
                concept_name = f"Multa Trabajo: {job_name} ({job_date.strftime('%d/%b/%Y').upper()})"
                
                # Verificar si ya existe
                cur.execute("""
                    SELECT 1 FROM bill_concept_item
                    WHERE water_bill_id = %s
                      AND concept_name LIKE %s
                    LIMIT 1
                """, (bill_id, f"%{job_name}%"))
                
                if not cur.fetchone():
                    concepts_to_add.append(('TRABAJO', concept_name, job_fine))
                else:
                    print(f"        ⊘ Concepto ya existe: {concept_name}")
            
            for meeting_id, meeting_name, meeting_fine, meeting_date in meeting_fines:
                concept_name = f"Multa Reunión: {meeting_name} ({meeting_date.strftime('%d/%b/%Y').upper()})"
                
                # Verificar si ya existe
                cur.execute("""
                    SELECT 1 FROM bill_concept_item
                    WHERE water_bill_id = %s
                      AND concept_name LIKE %s
                    LIMIT 1
                """, (bill_id, f"%{meeting_name}%"))
                
                if not cur.fetchone():
                    concepts_to_add.append(('REUNION', concept_name, meeting_fine))
                else:
                    print(f"        ⊘ Concepto ya existe: {concept_name}")
            
            if not concepts_to_add:
                print("      → Todos los conceptos ya existen")
                continue
            
            # 2.4. Agregar nuevos conceptos
            fines_total = Decimal('0.00')
            for fine_type, concept_name, concept_amount in concepts_to_add:
                concept_id = str(uuid.uuid4())
                
                cur.execute("""
                    INSERT INTO bill_concept_item (
                        bill_concept_item_id,
                        water_bill_id,
                        concept_name,
                        amount,
                        assigned_date,
                        active,
                        created_at
                    ) VALUES (%s, %s, %s, %s, %s, true, %s)
                """, (
                    concept_id,
                    bill_id,
                    concept_name,
                    concept_amount,
                    period_start,
                    datetime.now()
                ))
                
                fines_total += Decimal(str(concept_amount))
                print(f"        ✓ Agregado: {concept_name} (Bs {concept_amount})")
            
            # 2.5. Actualizar total de la factura
            new_total = Decimal(str(total_amount)) + fines_total
            
            cur.execute("""
                UPDATE water_bill
                SET total_amount = %s,
                    remaining_balance = %s,
                    updated_at = %s
                WHERE water_bill_id = %s
            """, (
                new_total,
                new_total,
                datetime.now(),
                bill_id
            ))
            
            total_fines_added += fines_total
            bills_updated += 1
            print(f"      ✓ Factura actualizada: {total_amount} + {fines_total} = {new_total} Bs")
        
        # Confirmar cambios
        conn.commit()
        
        print("\n" + "=" * 80)
        print(f"✅ MIGRACIÓN COMPLETADA")
        print(f"   • Facturas actualizadas: {bills_updated}")
        print(f"   • Multas agregadas: Bs {total_fines_added}")
        print("=" * 80)
        
    except Exception as e:
        conn.rollback()
        print(f"\n❌ Error durante la migración: {e}")
        import traceback
        traceback.print_exc()
    finally:
        cur.close()
        conn.close()

if __name__ == "__main__":
    migrate_suspended_partner_bills()
