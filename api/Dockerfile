FROM gradle:8.8-jdk21 AS builder

WORKDIR /app

COPY . .

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/build/quarkus-app/ /app/

EXPOSE 8080

CMD ["java", "-jar", "/app/quarkus-run.jar"]
