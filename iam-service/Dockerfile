# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Use the official Maven image with Java 21 (required by Spring Boot 4.x)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies separately.
# Docker caches this layer — if pom.xml hasn't changed, the download is skipped.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source and build the jar
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Use a smaller JRE-only image for the final container
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the built jar from the build stage (not Maven, not source)
COPY --from=build /app/target/iam-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
