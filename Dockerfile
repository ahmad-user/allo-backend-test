    # Multi-stage Dockerfile for Allo Bank Backend Challenge

    # ── Stage 1: Build ──────────────────────────────────────────────────────────
    FROM eclipse-temurin:21-jdk-alpine AS builder

    WORKDIR /app

    # Copy Maven wrapper and pom first for layer caching
    COPY .mvn/ .mvn/
    COPY mvnw pom.xml ./
    RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

    # Copy source and build package
    COPY src/ src/
    RUN ./mvnw package -DskipTests -q

    # ── Stage 2: Runtime ─────────────────────────────────────────────────────────
    FROM eclipse-temurin:21-jre-alpine AS runtime

    WORKDIR /app

    COPY --from=builder /app/target/*.jar app.jar

    EXPOSE 4110

    ENTRYPOINT ["java", "-jar", "app.jar"]
