FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests -Dmaven.wagon.http.retryHandler.count=5 package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/gif-maker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
