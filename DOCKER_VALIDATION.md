# Docker验证流程

## 📋 概述

本文档定义了每个迭代完成后必须执行的Docker验证流程，确保功能正常运行并进行基础回归测试。

---

## 🚀 标准验证流程

### 1. 构建Docker镜像

```bash
# 在项目根目录执行
cd /Users/simon/trae_solo/stock_v2

# 清理旧容器和镜像（可选）
docker-compose down --rmi all

# 构建并启动所有服务
docker-compose up --build -d

# 查看日志确认启动
docker-compose logs -f
```

### 2. 等待服务启动

```bash
# 等待所有服务就绪（建议等待30-60秒）
sleep 60

# 检查服务状态
docker-compose ps
```

预期输出：
```
NAME                IMAGE                  COMMAND                  SERVICE      CREATED        STATUS
stock-akshare       stock-akshare-service   "python akshare_ser…"   akshare     X minutes ago  Up
stock-redis         redis:7-alpine          "docker-entrypoint.s…"   redis       X minutes ago  Up
stock-backend       stock-backend           "java -jar app.jar"      backend     X minutes ago  Up
stock-frontend      stock-frontend          "/docker-entrypoint.…"   frontend    X minutes ago  Up
```

### 3. 健康检查

```bash
# 检查Python微服务
curl -s http://localhost:5001/health

# 检查Spring Boot后端
curl -s http://localhost:8080/api/quote/600519

# 检查前端
curl -s http://localhost:3000 | grep -q "vue\|html" && echo "Frontend OK"
```

---

## ✅ 迭代3/4验证清单（舆情增强版）

### 核心功能验证

- [ ] **Python微服务启动成功**（端口5001）
- [ ] **东方财富新闻API调用成功**
  ```bash
  curl -s http://localhost:5001/api/news/600519
  ```
- [ ] **情感分析功能正常**
  ```bash
  curl -s http://localhost:5001/api/sentiment/600519
  ```
- [ ] **Spring Boot后端启动成功**（端口8080）
- [ ] **完整分析流程返回结果**
  ```bash
  curl -X POST http://localhost:8080/api/analysis \
    -H "Content-Type: application/json" \
    -d '{"stockCode":"600519"}' \
    | jq '.result.sentiment'
  ```
- [ ] **新闻数据包含真实新闻**
- [ ] **情感统计字段存在**（positiveCount、negativeCount、neutralCount）

### 回归测试

- [ ] **K线数据正常**（来自AKShare）
- [ ] **实时行情正常**
- [ ] **技术指标计算正常**（MACD/RSI）
- [ ] **投资建议生成正常**

---

## 🔧 迭代验证流程（通用）

每个迭代完成后，执行以下步骤：

### Step 1: 代码审查
- [ ] 所有修改已提交到Git
- [ ] 分支命名规范：`feature/iteration{N}-{feature}`
- [ ] PR创建或直接推送到功能分支

### Step 2: 本地测试
- [ ] 本地服务启动成功
- [ ] 所有新功能已测试
- [ ] 回归测试通过

### Step 3: Docker构建
- [ ] 镜像构建成功（无错误）
- [ ] 所有服务容器启动成功
- [ ] 容器健康检查通过

### Step 4: Docker验证
- [ ] 新功能在Docker环境中正常工作
- [ ] API端点响应正确
- [ ] 数据流正确（前端→后端→Python微服务）

### Step 5: 回归测试
- [ ] 已完成功能仍然正常
- [ ] 性能无明显下降
- [ ] 无新增错误日志

### Step 6: 文档更新
- [ ] SPEC.md更新完成
- [ ] API文档更新（如有新增）
- [ ] README快速开始指南（如需要）

---

## 🐛 常见问题排查

### 1. 容器启动失败

```bash
# 查看容器日志
docker-compose logs <service-name>

# 常见问题：
# - 端口已被占用：修改docker-compose.yml中的端口映射
# - 依赖服务未启动：使用docker-compose up -d重新启动
# - 镜像构建失败：查看构建日志，修复Dockerfile
```

### 2. Python微服务无法访问

```bash
# 检查容器内服务
docker exec -it stock-akshare python -c "import akshare; print('AKShare OK')"

# 检查端口绑定
docker exec -it stock-akshare netstat -tlnp | grep 5001

# 检查网络连接
docker exec -it stock-backend ping akshare-service
```

### 3. 后端无法连接Python微服务

```bash
# 检查环境变量
docker exec -it stock-backend env | grep AKSHARE

# 检查网络配置
docker network inspect stock_v2_stock-network

# 测试连接
docker exec -it stock-backend curl http://akshare-service:5001/health
```

### 4. 前端无法访问后端API

```bash
# 检查Nginx配置（如果使用）
docker exec -it stock-frontend cat /etc/nginx/nginx.conf

# 检查后端健康状态
docker exec -it stock-backend curl localhost:8080/actuator/health
```

### 5. Redis连接失败

```bash
# 检查Redis日志
docker-compose logs redis

# 测试Redis连接
docker exec -it stock-backend redis-cli -h redis ping
```

---

## 📊 验证报告模板

每次验证完成后，记录以下信息：

```markdown
## 验证报告 - 迭代{N}

**日期**: YYYY-MM-DD HH:MM
**验证人**: AI Assistant
**环境**: Docker Desktop / Linux

### 构建状态
- [ ] 构建成功
- [ ] 所有容器启动成功

### 功能验证
| 功能 | 状态 | 备注 |
|------|------|------|
| 功能1 | ✅ 通过/❌ 失败 | 描述 |
| 功能2 | ✅ 通过/❌ 失败 | 描述 |

### 回归测试
| 功能 | 状态 | 备注 |
|------|------|------|
| K线数据 | ✅ 通过/❌ 失败 | 描述 |
| 技术分析 | ✅ 通过/❌ 失败 | 描述 |

### 性能测试
- 首次加载时间: X ms
- API响应时间: X ms
- 内存使用: X MB

### 问题与修复
| 问题 | 解决方案 | 状态 |
|------|---------|------|
| 问题1 | 解决方案 | 已修复 |

### 最终结论
- [ ] 验证通过，可以合并到主分支
- [ ] 需要修复后重新验证
```

---

## 🔄 自动化验证脚本

建议创建自动化验证脚本：

```bash
#!/bin/bash
# validate.sh - 自动化验证脚本

set -e

echo "=== 开始Docker验证 ==="

# 1. 构建并启动
echo "1. 构建Docker镜像..."
docker-compose up --build -d

# 2. 等待服务启动
echo "2. 等待服务启动..."
sleep 60

# 3. 健康检查
echo "3. 健康检查..."

# Python微服务
echo -n "Python微服务: "
curl -s http://localhost:5001/health && echo " OK" || echo " FAIL"

# 后端API
echo -n "后端API: "
curl -s http://localhost:8080/api/quote/600519 | grep -q "stockCode" && echo " OK" || echo " FAIL"

# 前端
echo -n "前端: "
curl -s http://localhost:3000 | grep -q "vue\|html" && echo " OK" || echo " FAIL"

# 4. 功能验证
echo "4. 功能验证..."

# 新闻API
echo -n "新闻API: "
curl -s http://localhost:5001/api/news/600519 | grep -q "title" && echo " OK" || echo " FAIL"

# 情感分析
echo -n "情感分析: "
curl -s http://localhost:5001/api/sentiment/600519 | grep -q "sentiment" && echo " OK" || echo " FAIL"

echo "=== 验证完成 ==="
```

---

## 📝 后续迭代验证计划

### 迭代5: AI初探版
- [ ] LLM API调用成功
- [ ] AI生成的投资建议合理
- [ ] 降级机制正常（API失败时）

### 迭代6: 向量检索版
- [ ] PostgreSQL + pgvector正常
- [ ] 相似性检索返回结果
- [ ] 历史分析正确存储

### 迭代7: 生产就绪版
- [ ] Nginx负载均衡正常
- [ ] Prometheus监控数据采集
- [ ] Grafana仪表盘可访问
- [ ] 熔断降级测试通过

---

*文档版本: v1.0*
*创建时间: 2026-05-16*
*最后更新: 2026-05-16*
