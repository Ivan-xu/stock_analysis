#!/bin/bash

cd "$(dirname "$0")"

echo "Starting Backend Server..."
cd backend

if [ -f "mvnw" ]; then
    chmod +x mvnw
    ./mvnw spring-boot:run
else
    if command -v mvn &> /dev/null; then
        mvn spring-boot:run
    else
        echo "Maven not found. Please install Maven or use Gradle."
        if [ -f "gradlew" ]; then
            chmod +x gradlew
            ./gradlew bootRun
        fi
    fi
fi
