# Multi-stage build로 이미지 크기 최소화

# --- Stage 1: 빌드 ---
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Gradle wrapper와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

# 각 모듈의 build.gradle만 먼저 복사 (의존성 캐싱)
COPY common/build.gradle common/
COPY api-server/build.gradle api-server/

# 의존성 다운로드 (이 단계가 캐시되면 빌드 빨라짐)
RUN ./gradlew :api-server:dependencies --no-daemon || true

# 소스 코드 복사
COPY common common
COPY api-server api-server

# 빌드 (테스트 제외)
RUN ./gradlew :api-server:bootJar --no-daemon -x test

# --- Stage 2: 실행 ---
FROM eclipse-temurin:21-jre-alpine

# 보안: non-root 사용자
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /workspace/api-server/build/libs/api-server.jar app.jar

# JVM 옵션
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]