# Use a base image with Java 21
FROM eclipse-temurin:21-alpine

ENV SPRING_PROFILES_ACTIVE = production

LABEL maintainer="dreamsbo"

# Set the working directory
WORKDIR /app

# Copy the JAR file to the container
COPY target/water-api-1.0.0.jar application.jar

# Copy reports to container
ADD /reports /app/reports

ADD /reports/fonts/freefont /usr/share/fonts/freefont

# Expose the port that your Spring Boot application listens on (default is 8080)
EXPOSE 8085

# Define the command to run your application
CMD ["java", "-jar", "application.jar"]
