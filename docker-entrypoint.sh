#!/bin/sh
set -eu

# Spring Boot's PostgreSQL driver expects a JDBC URL. Supabase's Connect
# dialog gives a libpq-style URI, so normalize it here if it was pasted into
# SPRING_DATASOURCE_URL. Credentials belong in the separate datasource
# username/password variables and are removed from the URI authority.
case "${SPRING_DATASOURCE_URL:-}" in
  postgres://*)
    datasource_uri="${SPRING_DATASOURCE_URL#postgres://}"
    ;;
  postgresql://*)
    datasource_uri="${SPRING_DATASOURCE_URL#postgresql://}"
    ;;
  *)
    datasource_uri=""
    ;;
esac

if [ -n "$datasource_uri" ]; then
  authority="${datasource_uri%%/*}"
  if [ "$authority" = "$datasource_uri" ]; then
    path_and_query=""
  else
    path_and_query="/${datasource_uri#*/}"
  fi
  host_and_port="${authority##*@}"
  SPRING_DATASOURCE_URL="jdbc:postgresql://${host_and_port}${path_and_query}"
  export SPRING_DATASOURCE_URL
fi

exec java -jar /app/app.jar "$@"
