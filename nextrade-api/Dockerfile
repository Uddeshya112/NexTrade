FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/nextrade-api-1.0.0-SNAPSHOT.jar app.jar
USER 10001
ENTRYPOINT ["java","-jar","/app/app.jar"]
