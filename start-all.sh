#!/bin/bash

cd "$(dirname "$0")"

echo "================================"
echo "股票智能分析系统 - MVP"
echo "================================"
echo ""

echo "1. Starting Backend (Spring Boot on port 8080)..."
cd backend
if [ -f "pom.xml" ]; then
    if command -v mvn &> /dev/null; then
        mvn spring-boot:run &
    else
        echo "Maven not found. Please install Maven."
        exit 1
    fi
else
    if [ -f "gradlew" ]; then
        ./gradlew bootRun &
    fi
fi

cd ..

sleep 5

echo ""
echo "2. Starting Frontend (Vite on port 3000)..."
cd frontend

if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

npm run dev

echo ""
echo "================================"
echo "系统已启动!"
echo "前端: http://localhost:3000"
echo "后端: http://localhost:8080"
echo "H2控制台: http://localhost:8080/h2-console"
echo "================================"
