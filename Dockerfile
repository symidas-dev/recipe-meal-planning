FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline -B || true

COPY src/ src/

RUN ./mvnw clean package -DskipTests -B

RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination target/extracted || \
    cp target/*.jar target/app.jar

FROM eclipse-temurin:21-jre-jammy AS runner

RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV SERVER_PORT=8080

EXPOSE 8080

COPY --from=builder --chown=spring:spring /build/target/recipe-meal-planning-*.jar /app/app.jar

USER spring:spring

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
