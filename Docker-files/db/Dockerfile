FROM mysql:8.0.33

LABEL org.opencontainers.image.title="VProfile database" \
      org.opencontainers.image.description="MySQL database initialized with the VProfile schema"

# The compose configuration supplies MYSQL_ROOT_PASSWORD at runtime.
ENV MYSQL_DATABASE=accounts

# The official MySQL entrypoint runs this script when the data volume is first created.
COPY db_backup.sql /docker-entrypoint-initdb.d/01-db_backup.sql
