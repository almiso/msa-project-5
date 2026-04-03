FROM alpine:3.19
RUN apk add --no-cache curl
COPY run-etl.sh /run-etl.sh
RUN chmod +x /run-etl.sh
ENTRYPOINT ["/run-etl.sh"]
