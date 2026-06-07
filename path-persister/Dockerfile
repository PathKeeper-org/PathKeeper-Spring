FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY common/build.gradle common/
COPY api-server/build.gradle api-server/
COPY location-processor/build.gradle location-processor/
COPY alert-sender/build.gradle alert-sender/
COPY path-persister/build.gradle path-persister/

RUN ./gradlew :path-persister:dependencies --no-daemon || true

COPY common common
COPY path-persister path-persister

RUN ./gradlew :path-persister:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=builder /workspace/path-persister/build/libs/path-persister.jar app.jar

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
    CMD wget -q --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]