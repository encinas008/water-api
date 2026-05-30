#!/bin/bash

# Script para ejecutar la migración de multas a facturas de socios suspendidos
# Uso: ./add_fines_to_suspended_bills.sh

echo "╔════════════════════════════════════════════════════════════════════════════════╗"
echo "║       Migración: Agregar multas a facturas de socios SUSPENDIDOS             ║"
echo "╚════════════════════════════════════════════════════════════════════════════════╝"
echo ""

# Verificar si Python 3 está disponible
if ! command -v python3 &> /dev/null; then
    echo "❌ Error: Python 3 no está instalado"
    exit 1
fi

# Verificar si psycopg2 está disponible
python3 -c "import psycopg2" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "❌ Error: psycopg2 no está instalado"
    echo "   Instala con: pip install psycopg2-binary"
    exit 1
fi

# Ejecutar el script de migración
cd "$(dirname "$0")" || exit
python3 add_fines_to_suspended_bills.py

echo ""
echo "ℹ️  Para verificar los cambios, ejecuta:"
echo "   SELECT * FROM bill_concept_item WHERE concept_name LIKE 'Multa%' LIMIT 10;"
