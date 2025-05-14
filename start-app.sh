#!/bin/bash

# Make sure the environment variables are available to the application
echo "Starting the CelinePokedex application..."

# Make the script executable
chmod +x ./mvnw

# Start the Spring Boot application
./mvnw spring-boot:run