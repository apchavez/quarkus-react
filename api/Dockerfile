FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/quarkus-app/ /app/

EXPOSE 8080

CMD ["java", "-jar", "/app/quarkus-run.jar"]