FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="abuianova"

ENV TZ=UTC

WORKDIR /app

ARG JAR_FILE=stats-app/target/stats-app-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]