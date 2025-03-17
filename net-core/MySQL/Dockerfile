FROM mysql:8.0.40

COPY  *.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
HEALTHCHECK --interval=3s --retries=30  CMD exit 0
