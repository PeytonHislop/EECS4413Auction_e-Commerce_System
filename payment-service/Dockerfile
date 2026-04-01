# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/payment-service-1.0.0.jar app.jar

# Payment.db is SQLite — persist it with a volume
VOLUME /app/data

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar", \
            "--spring.datasource.url=jdbc:sqlite:/app/data/payment.db"]
