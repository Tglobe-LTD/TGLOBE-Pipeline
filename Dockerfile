# Build Stage
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Final Stage
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar tglobe-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tglobe-app.jar"]
