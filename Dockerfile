# Use the official OpenJDK image for Java 17 (multi-platform compatible)
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file (ensure it is built with Maven)
COPY target/road-sign-classification-0.0.1-SNAPSHOT.jar app.jar

# Copy the model file (ONNX model) to the same directory
COPY src/main/resources/models/gtsrb-model.onnx src/main/resources/models/gtsrb-model.onnx

# Expose the default Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
# Use the official OpenJDK image for Java 17 (multi-platform compatible)
FROM --platform=linux/amd64 openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file (ensure it is built with Maven)
COPY target/road-sign-classification-0.0.1-SNAPSHOT.jar app.jar

# Copy the model file (ONNX model) to the same directory
COPY src/main/resources/models/gtsrb-model.onnx src/main/resources/models/gtsrb-model.onnx

# Expose the default Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]