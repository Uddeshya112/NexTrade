FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -DskipTests -pl nextrade-api -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/nextrade-api/target/nextrade-api-1.0.0-SNAPSHOT-exec.jar app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod 0555 /app/docker-entrypoint.sh
ENV SPRING_PROFILES_ACTIVE=prod
USER 10001
EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
