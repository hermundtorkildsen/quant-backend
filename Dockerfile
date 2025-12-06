## Dockerfile for quant-backend (Spring Boot + Java 17)

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven config and source
COPY pom.xml .
COPY src ./src

# Build the Spring Boot JAR (skip tests to speed up CI/deploy)
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port (Render/other PaaS will typically set $PORT)
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

