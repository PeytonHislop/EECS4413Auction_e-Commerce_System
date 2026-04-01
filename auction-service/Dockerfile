# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/auction-service-1.0.0.jar app.jar

# This directory is where the SQLite database file will live.
# It gets mounted as a Docker volume so data survives container restarts.
VOLUME /app/data

EXPOSE 8082

# Pass the DB path and IAM URL as overrides so they work inside Docker networking.
# SERVICE_IAM_MOCK can be set to "true" in docker-compose for standalone testing.
ENTRYPOINT ["java", "-jar", "app.jar", \
            "--spring.datasource.url=jdbc:sqlite:/app/data/auction-service.db"]
