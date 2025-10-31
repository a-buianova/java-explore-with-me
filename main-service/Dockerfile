FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="abuianova"

ENV TZ=UTC

WORKDIR /app

ARG JAR_FILE=target/main-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]