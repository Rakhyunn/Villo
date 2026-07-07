# ── 빌드 스테이지 ──
FROM gradle:jdk21-graal-jammy AS builder
WORKDIR /app

# 래퍼 + 빌드 스크립트 먼저 복사 → 의존성 레이어 캐시 (소스만 바뀌면 재다운로드 안 함)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 복사 후 실행 jar만 빌드
# build 가 아니라 bootJar → *-plain.jar 이 안 생겨 아래 COPY *.jar 충돌 방지
# -x test → 이미지 빌드 시 DB 필요한 테스트 제외
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# ── 실행 스테이지 ──
# JVM 실행(native-image 아님)이라 풀 GraalVM 대신 가벼운 JRE 사용
FROM ghcr.io/graalvm/jdk-community:21
WORKDIR /app

# 빌드 스테이지에서 만든 실행 jar 복사 (bootJar라 1개만 존재)
COPY --from=builder /app/build/libs/*.jar app.jar

# 기본 프로파일 prod (compose 의 SPRING_PROFILES_ACTIVE 로 덮어쓰기 가능)
ENV SPRING_PROFILES_ACTIVE=prod
# 힙 제한은 compose 의 JAVA_TOOL_OPTIONS(-Xmx384m)로 주입

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
