# Stage 1: Build (Using a reliable Maven image)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime (Using the modern, secure Temurin Alpine image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy only the JAR from the build stage
COPY --from=build /app/target/*.jar tglobe-app.jar
EXPOSE 8080
# Run as a non-root user (Mastery Security Move)
RUN addgroup -S tglobe && adduser -S tglobe -G tglobe
USER tglobe
ENTRYPOINT ["java", "-jar", "tglobe-app.jar"]
