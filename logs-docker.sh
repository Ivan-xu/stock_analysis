#!/bin/bash

cd "$(dirname "$0")"

echo "查看日志 (Ctrl+C 退出)..."
docker-compose logs -f
