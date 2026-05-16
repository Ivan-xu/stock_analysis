#!/bin/bash

cd "$(dirname "$0")"

echo "================================"
echo "股票智能分析系统 - 数据增强版"
echo "================================"
echo ""

echo "1. 检查AKShare Python服务..."
if ! curl -s http://localhost:5001/health &> /dev/null; then
    echo "   启动AKShare数据服务 (端口5001)..."
    cd backend
    chmod +x start-akshare.sh
    ./start-akshare.sh &
    cd ..
    sleep 3
else
    echo "   ✓ AKShare服务已运行"
fi

echo ""
echo "2. 检查Redis..."
if ! redis-cli ping &> /dev/null; then
    echo "   ⚠ Redis未运行，请先启动Redis: redis-server"
else
    echo "   ✓ Redis已运行"
fi

echo ""
echo "3. 启动后端 (Spring Boot on port 8080)..."
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
echo "4. 启动前端 (Vite on port 3000)..."
cd frontend

if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

npm run dev

echo ""
echo "================================"
echo "系统已启动!"
echo "================================"
echo "AKShare服务: http://localhost:5001"
echo "前端:       http://localhost:3000"
echo "后端:       http://localhost:8080"
echo "Redis:      localhost:6379"
echo "================================"
