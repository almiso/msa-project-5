#!/bin/sh

# Завершаем скрипт при любой ошибке
set -e
echo "[$(date +'%Y-%m-%d %H:%M:%S')] [INFO] Starting daily export of 'shipments' table..."

# Получаем доступы к БД из переменных окружения
DATABASE=${DB_NAME:-logistics}
USERNAME=${DB_USER:-admin}
PASSWORD=${DB_PASSWORD:-admin_pwd}
HOST=${DB_HOST:-postgres-service}
PORT=${DB_PORT:-5432}

# Формируем имя файла с текущей датой
OUTPUT_FILE="/tmp/shipments_$(date +%Y%m%d).csv"

# Передаем пароль для psql через переменную
export PGPASSWORD=$PASSWORD
CONN_STR="host=$HOST port=$PORT dbname=$DATABASE user=$USERNAME"

echo "[$(date +'%Y-%m-%d %H:%M:%S')] [INFO] Executing PostgreSQL COPY command..."

# Выгружаем данные напрямую в CSV формат
psql "$CONN_STR" -c "\copy (SELECT * FROM shipments) TO '$OUTPUT_FILE' DELIMITER ',' CSV HEADER;"

if [ $? -eq 0 ]; then
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [SUCCESS] Data successfully exported to $OUTPUT_FILE"
    
    # Выводим инфо о файле для логов
    ls -lh $OUTPUT_FILE
    echo "--- FILE PREVIEW ---"
    head -n 5 $OUTPUT_FILE
    echo "--------------------"
    
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [INFO] Simulating upload to specialized analysts storage (S3/GCS)..."
    sleep 2 # Делаем вид, что отправляем файл
    
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [SUCCESS] Upload complete. Dashboard pipelines will be triggered automatically."
    exit 0
else
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [ERROR] Data export failed!"
    exit 1
fi
