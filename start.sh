#!/bin/bash

# Load environment variables from .env file if it exists
if [ -f .env ]; then
    echo "Loading .env file..."
    export SUMMARY_SCHEDULE_CRON=$(grep 'SUMMARY_SCHEDULE_CRON=' .env | cut -d'=' -f2)
fi

# Start the Spring Boot application
echo "Starting GitHub Trending Bot..."
java -jar target/spring-bot-0.0.1-SNAPSHOT.jar
