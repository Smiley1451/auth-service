# ---- Step 1: Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy source code and pom.xml
COPY . .

# Build the application and skip tests
RUN ./mvnw clean package -DskipTests

# ---- Step 2: Runtime Stage ----
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the jar from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose application port (update if different)
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
