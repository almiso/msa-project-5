# Базовый легковесный образ alpine
FROM alpine:3.19

# Ставим только нужные утилиты для работы с postgres
RUN apk update && \
    apk add --no-cache postgresql-client tzdata && \
    rm -rf /var/cache/apk/*

WORKDIR /app

# Копируем наш bash-скрипт
COPY export.sh /app/export.sh

# Выдаем права на исполнение
RUN chmod +x /app/export.sh

# Запускаем скрипт при старте контейнера
ENTRYPOINT ["/app/export.sh"]
