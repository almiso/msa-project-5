#!/bin/sh
# Клиент для вызова ETL Job через REST API
# Ждём пока приложение поднимется, затем отправляем POST-запрос

echo "=== ETL Client: waiting for app to start ==="

# Ждём готовности app (до 60 секунд)
for i in $(seq 1 30); do
  if curl -sf http://app:8080/actuator/health > /dev/null 2>&1; then
    echo "App is ready!"
    break
  fi
  echo "Waiting... ($i/30)"
  sleep 2
done

echo ""
echo "=== ETL Client: launching Job ==="
echo ""

# Вызываем API с логированием
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://app:8080/api/run-etl \
  -H "Content-Type: application/json")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)

echo "HTTP Status: $HTTP_CODE"
echo "Response: $BODY"
echo ""
echo "=== ETL Client: done ==="
