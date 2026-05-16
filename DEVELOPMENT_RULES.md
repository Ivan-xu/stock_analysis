# 开发规范与工作流程

## 📌 重要规则：每个迭代完成后必须进行Docker验证

### 规则说明

**核心要求**：
> 每个迭代完成后，必须启动Docker来验证迭代相关的功能，以及做基础功能的回归测试。

### 验证流程

每个迭代完成后，执行以下步骤：

#### 1. 停止本地服务
```bash
# 停止所有本地运行的服务
lsof -ti:5001 | xargs kill -9  # Python微服务
lsof -ti:8080 | xargs kill -9  # Spring Boot
lsof -ti:3000 | xargs kill -9  # Vue前端
```

#### 2. 构建并启动Docker
```bash
cd /Users/simon/trae_solo/stock_v2
docker-compose up --build -d
```

#### 3. 等待服务启动
```bash
# 建议等待60秒
sleep 60
docker-compose ps  # 确认所有容器状态为Up
```

#### 4. 健康检查
```bash
# Python微服务
curl http://localhost:5001/health

# Spring Boot后端
curl http://localhost:8080/api/quote/600519

# Vue前端
curl http://localhost:3000
```

#### 5. 功能验证
根据当前迭代新增的功能，执行对应的验证清单：
- [ ] 核心新功能正常工作
- [ ] API端点响应正确
- [ ] 数据流完整（前端→后端→Python微服务）

#### 6. 回归测试
- [ ] 已完成功能仍然正常
- [ ] 无新增错误
- [ ] 性能无明显下降

#### 7. 记录验证报告
创建验证报告，记录：
- 验证时间
- 构建状态
- 功能验证结果
- 回归测试结果
- 问题与解决方案

---

## 📝 迭代验证清单模板

### 迭代{N}验证报告

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
| 新闻API | ✅ 通过/❌ 失败 | 描述 |
| 情感分析 | ✅ 通过/❌ 失败 | 描述 |

### 问题与修复
| 问题 | 解决方案 | 状态 |
|------|---------|------|
| 问题1 | 解决方案 | 已修复 |

### 最终结论
- [ ] 验证通过，可以继续下一个迭代
- [ ] 需要修复后重新验证

---

## 🔧 已知问题

### Docker构建问题

**问题1**：Docker Compose显示容器已创建但未启动
- 原因：需要使用`docker-compose up --build`而非仅`up`
- 解决：使用完整构建命令

**问题2**：前端构建使用npm install而不是生产构建
- 解决：修改Dockerfile使用`npm run build`

### 端口占用
- 5001：Python微服务
- 6379：Redis
- 8080：Spring Boot
- 3000：Vue前端（Nginx）

---

## 📋 下一步迭代计划

根据SPEC.md，下一个迭代是：

### 迭代5：AI初探版
- **核心目标**：集成LLM能力，让AI参与股票分析
- **关键技术**：SpringAI + 智谱AI (GLM-4)
- **预计工时**：3-4天

### 迭代6：向量检索版
- **核心目标**：使用PostgreSQL + pgvector实现历史分析复用
- **关键技术**：向量嵌入 + 相似性检索
- **预计工时**：3-4天

### 迭代7：生产就绪版
- **核心目标**：Docker容器化 + 监控
- **关键技术**：Docker Compose + Prometheus + Grafana
- **预计工时**：4-5天

---

## 📚 参考文档

- 完整Docker验证流程：`DOCKER_VALIDATION.md`
- 系统规格说明：`SPEC.md`
- Git仓库：`feature/iteration3-sentiment` 分支

---

*文档版本: v1.0*
*创建时间: 2026-05-16*
*最后更新: 2026-05-16*
*规则制定者: 用户要求*
