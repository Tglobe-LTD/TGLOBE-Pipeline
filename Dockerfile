# Final Stage only - Use Java 21 runtime to match your build
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the JAR we will build on your Windows machine
COPY target/*.jar tglobe-app.jar
EXPOSE 8080
# Security: Run as non-root
RUN addgroup -S tglobe && adduser -S tglobe -G tglobe
USER tglobe
ENTRYPOINT ["java", "-jar", "tglobe-app.jar"]