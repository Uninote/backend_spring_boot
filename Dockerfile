# Use an OpenJDK image as the base
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the target directory to the container's /app directory
COPY target/your-app-name-1.0-SNAPSHOT.jar /app/app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "/app/app.jar"]
