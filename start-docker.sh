#!/bin/bash

set -e

cd "$(dirname "$0")"

echo "==================================="
echo "股票智能分析系统 - Docker部署"
echo "==================================="
echo ""

echo "1. 检查Docker环境..."
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "错误: Docker服务未运行"
    exit 1
fi

echo "✓ Docker环境正常"
echo ""

echo "2. 停止旧容器..."
docker-compose down 2>/dev/null || true
echo "✓ 已停止旧容器"
echo ""

echo "3. 构建Docker镜像..."
docker-compose build --no-cache
echo "✓ 镜像构建完成"
echo ""

echo "4. 启动所有服务..."
docker-compose up -d
echo ""

echo "5. 等待服务启动..."
sleep 10

echo "6. 检查服务状态..."
docker-compose ps
echo ""

echo "==================================="
echo "系统启动完成！"
echo "==================================="
echo "AKShare服务: http://localhost:5001"
echo "后端API:     http://localhost:8080"
echo "前端界面:    http://localhost:3000"
echo "Redis:       localhost:6379"
echo ""
echo "查看日志: docker-compose logs -f"
echo "停止服务: docker-compose down"
echo "==================================="
