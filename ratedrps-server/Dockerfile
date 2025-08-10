# Use a lightweight OpenJDK image
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the fat JAR into the container
COPY target/ratedrps-server-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on (default 8080)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
