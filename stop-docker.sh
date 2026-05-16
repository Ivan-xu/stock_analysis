#!/bin/bash

cd "$(dirname "$0")"

echo "停止Docker容器..."
docker-compose down

echo "清理完成"
