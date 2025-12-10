FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY . .

RUN dos2unix gradlew || true
RUN chmod 755 gradlew

ENV GRADLE_USER_HOME=/app/.gradle
ENV GRADLE_OPTS="-Dorg.gradle.caching=false"

RUN ./gradlew clean build -x test --no-daemon

# --- SEGUNDO ESTÁGIO (RUNTIME) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# ADICIONE ESTA LINHA AQUI 👇
# libgcc: a lib que está faltando no erro.
# gcompat: garante compatibilidade extra para binários que esperam glibc (comum no Netty).
RUN apk add --no-cache libgcc gcompat

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]