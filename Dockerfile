# Use a lightweight official JDK 17 runtime
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the Spring Boot fat JAR built by Maven/IntelliJ
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose the app port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
