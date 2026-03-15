# 1단계: 빌드 환경 (코드 복사 및 빌드)
FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build --no-daemon -x test

# 2단계: 실행 환경 (빌드된 결과물만 가져와서 실행)
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
# 빌더 단계에서 생성된 jar 파일만 가져옴
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]