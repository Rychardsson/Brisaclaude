# ─── Etapa 1: Build ────────────────────────────────────────────────────────
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

# Copia apenas o pom.xml primeiro para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o restante e compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Etapa 2: Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria usuário não-root para segurança
RUN addgroup -S brisa && adduser -S brisa -G brisa

# Copia o JAR gerado
COPY --from=build /app/target/brisa-0.0.1-SNAPSHOT.jar app.jar

# Ajusta permissões
RUN chown brisa:brisa app.jar
USER brisa

EXPOSE 8082

# Variáveis de ambiente com valores padrão (podem ser sobrescritos no Compose)
ENV SERVER_PORT=8082 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
