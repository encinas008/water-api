#!/bin/bash

# Script para ejecutar la migración de datos de SQLite a PostgreSQL en el orden correcto
# Basado en la dependencia de entidades JPA y lógica de los scripts de migración.

# Detener el script si ocurre un error
set -e

# Configuración de colores para la salida
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===== Iniciando Proceso de Migración Ordenada =====${NC}"

# 1. Verificar entorno virtual
if [ -d "venv" ]; then
    echo -e "${GREEN}Activando entorno virtual...${NC}"
    source venv/bin/activate
else
    echo "Advertencia: No se encontró la carpeta venv. Asegúrate de tener las dependencias instaladas."
fi

# El orden de ejecución es crítico debido a las llaves foráneas y dependencias de datos:

# 0. Asegurar Datos de Catálogo (Estado PARTIAL_PAID, etc)
echo -e "${BLUE}[0/8] Verificando datos de catálogo...${NC}"
python3 migration/ensure_catalog_data.py

# 1. Migrar Socios (Entidad base)
echo -e "${BLUE}[1/8] Migrando Socios...${NC}"
python3 migration/migrate_partners.py

# 2. Migrar Trabajos (Independiente de facturas, pero necesita socios para asistencias)
echo -e "${BLUE}[2/8] Migrando Trabajos...${NC}"
python3 migration/migrate_jobs.py

# 3. Migrar Reuniones (Independiente de facturas, pero necesita socios para asistencias)
echo -e "${BLUE}[3/8] Migrando Reuniones...${NC}"
python3 migration/migrate_meetings.py

# 4. Migrar Lecturas y Facturas (Genera la estructura de cobro de agua)
echo -e "${BLUE}[4/8] Migrando Lecturas y Facturas...${NC}"
python3 migration/migrate_readings_and_bills.py

# 5. Vincular Multas a Facturas (Utiliza facturas y asistencias para añadir conceptos de cobro)
echo -e "${BLUE}[5/8] Vinculando Multas de Asistencia a Facturas...${NC}"
python3 migration/migrate_bill_concepts_jobs_meetings.py

# 6. Migrar Pagos (El paso final, vincula pagos a facturas y asistencias específicas)
echo -e "${BLUE}[6/8] Migrando Pagos...${NC}"
python3 migration/migrate_payments.py

# 7. Configurar Secuencias (Post-migración para permitir nuevos registros)
echo -e "${BLUE}[7/8] Configurando secuencias de base de datos...${NC}"
python3 migration/setup_sequences.py

echo -e "${GREEN}===== Proceso de Migración Completado con Éxito =====${NC}"
