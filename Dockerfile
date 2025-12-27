# --- Estágio de Build ---
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# 1. OTIMIZAÇÃO DE CACHE: Copia só os arquivos de configuração primeiro
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Arruma permissões do Windows
RUN dos2unix gradlew || true
RUN chmod +x gradlew

# Baixa as dependências ANTES de copiar o código fonte
# Isso faz o Docker salvar essa camada em cache. Se você só mudar código, não baixa internet de novo.
RUN ./gradlew dependencies --no-daemon || true

# 2. Agora sim copia o código fonte
COPY src src

# Builda o projeto
RUN ./gradlew clean build -x test --no-daemon

# --- Estágio Final (Produção) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Instala libs de compatibilidade e TIMEZONE (Importante pro Brasil)
RUN apk add --no-cache libgcc gcompat tzdata

# Define o horário do servidor para o de Recife (pra log e banco baterem certo)
ENV TZ=America/Recife

# 3. SEGURANÇA NA CÓPIA: Evita copiar o 'plain.jar' por engano
# O comando abaixo procura pelo JAR principal que não termina com -plain.jar
RUN cp $(find /app/build/libs/ -name "*.jar" ! -name "*-plain.jar") app.jar

# Configurações Finais
EXPOSE 8080

# Usamos 'sh -c' para permitir passar configurações de memória via variável de ambiente (JAVA_OPTS) se precisar no futuro
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]