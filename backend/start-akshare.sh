#!/bin/bash

echo "启动AKShare数据服务..."

# 检查Python3是否可用
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到Python3，请先安装Python 3.8+"
    exit 1
fi

# 检查pip是否可用
if ! command -v pip3 &> /dev/null; then
    echo "错误: 未找到pip3，请先安装pip"
    exit 1
fi

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 检查依赖是否已安装
if ! pip3 show akshare &> /dev/null; then
    echo "安装AKShare依赖..."
    pip3 install -r requirements.txt
fi

# 检查端口5001是否被占用
if lsof -Pi :5001 -sTCP:LISTEN -t &> /dev/null; then
    echo "警告: 端口5001已被占用，AKShare服务可能已在运行"
    echo "尝试连接测试..."
    curl -s http://localhost:5001/health
    echo ""
else
    echo "启动AKShare服务在端口5001..."
    echo "按Ctrl+C停止服务"
    python3 akshare_service.py
fi
