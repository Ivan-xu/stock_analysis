# 迭代2：数据增强版 - 快速开始指南

## 🚀 新增功能

- ✅ **真实K线数据**：通过AKShare获取真实历史K线
- ✅ **实时行情**：获取当前股价、涨跌幅、成交量
- ✅ **Redis缓存**：热点数据缓存，减少API调用
- ✅ **MySQL持久化**：支持数据持久化（可选）

---

## 📋 环境要求

### 必须安装
- **Python 3.8+**
- **Maven 3.6+**
- **Node.js 16+**
- **Redis 6+** (可选，用于缓存)

### Python依赖
```bash
pip3 install flask flask-cors akshare pandas requests
```

### Redis安装
```bash
# macOS
brew install redis
brew services start redis

# Ubuntu/Debian
sudo apt update
sudo apt install redis-server
sudo systemctl start redis
```

---

## 🛠️ 启动步骤

### 方式1：使用一键启动脚本（推荐）
```bash
cd /Users/simon/trae_solo/stock_v2

# 确保Redis已启动
redis-server

# 一键启动所有服务
./start-all.sh
```

### 方式2：手动启动

#### 1. 启动AKShare Python服务
```bash
cd backend
chmod +x start-akshare.sh
./start-akshare.sh
```

验证服务是否启动：
```bash
curl http://localhost:5001/health
# 应返回: {"status": "ok"}
```

#### 2. 启动Spring Boot后端
```bash
cd backend
mvn spring-boot:run
```

#### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```

---

## 🌐 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| AKShare数据服务 | 5001 | Python微服务，提供真实行情 |
| Spring Boot后端 | 8080 | Java API服务 |
| 前端Vue应用 | 3000 | 用户界面 |
| Redis | 6379 | 缓存服务（可选） |

---

## 📊 API测试

### 测试K线数据
```bash
curl http://localhost:5001/api/kline/600519
```

### 测试实时行情
```bash
curl http://localhost:5001/api/quote/600519
```

### 测试Java后端
```bash
curl http://localhost:8080/api/quote/600519
```

---

## 🐳 Docker启动（可选）

如果使用Docker Compose：

```bash
docker-compose up -d
```

---

## ⚠️ 注意事项

1. **AKShare服务必须先启动**：后端依赖Python服务提供数据
2. **网络要求**：AKShare需要访问网络获取数据
3. **限流处理**：AKShare有API限流，缓存可减少请求次数
4. **数据延迟**：实时行情可能有几秒延迟

---

## 🔧 常见问题

### Q: AKShare服务启动失败？
A: 检查Python依赖是否安装正确：
```bash
pip3 install flask flask-cors akshare pandas requests
```

### Q: Redis连接失败？
A: 确保Redis已启动：
```bash
redis-cli ping
# 应返回: PONG
```

### Q: 行情数据获取失败？
A: 检查网络连接，AKShare需要访问互联网获取数据

---

## 📝 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Vue 3)                          │
│                    localhost:3000                        │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot (Java 21)                      │
│              localhost:8080                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │  MarketDataService (Redis缓存 + 回退AKShare)    │   │
│  │  ResearcherAgent → TechAnalystAgent            │   │
│  │  SentimentAgent → InvestmentManagerAgent      │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                    │                           │
                    ▼                           ▼
┌─────────────────────────┐       ┌─────────────────────────┐
│   Redis (缓存)          │       │   AKShare Python        │
│   localhost:6379        │       │   localhost:5001        │
└─────────────────────────┘       └─────────────────────────┘
                                            │
                                            ▼
                                    ┌─────────────────────────┐
                                    │   东方财富/新浪财经     │
                                    │   (真实行情数据)        │
                                    └─────────────────────────┘
```

---

## 📚 相关文档

- [SPEC.md](file:///Users/simon/trae_solo/stock_v2/SPEC.md) - 完整规格说明书
- [GIT_WORKFLOW.md](file:///Users/simon/trae_solo/stock_v2/GIT_WORKFLOW.md) - Git工作流程
- [backend/README.md](file:///Users/simon/trae_solo/stock_v2/backend/README.md) - 后端说明

---

*迭代2完成时间: 2026-05-16*
