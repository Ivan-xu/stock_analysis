# Git分支策略与工作流程

## 📋 项目迭代与分支规划

### 7个迭代的完整Git工作流

| 迭代 | 分支名 | 核心功能 | 合并时机 |
|------|--------|----------|----------|
| **I1** | `master` | MVP基础版 | ✅ 已完成（当前） |
| **I2** | `feature/iteration2-data` | 数据增强版 | I2完成后合并 |
| **I3** | `feature/iteration3-ai` | AI初探版 | I3完成后合并 |
| **I4** | `feature/iteration4-sentiment` | 舆情增强版 | I4完成后合并 |
| **I5** | `feature/iteration5-debate` | 多模型辩论版 | I5完成后合并 |
| **I6** | `feature/iteration6-vector` | 向量检索版 | I6完成后合并 |
| **I7** | `feature/iteration7-production` | 生产就绪版 | I7完成后合并 |

---

## 🚀 Git工作流程（推荐）

### 1. 当前状态（I1完成）
```bash
# 当前已在master分支，包含I1: MVP基础版
git log --oneline
```

### 2. 开始新迭代（以I2为例）

#### 创建并切换到功能分支
```bash
# 从master创建I2分支
git checkout -b feature/iteration2-data master

# 验证当前分支
git branch
```

#### 开发迭代功能
```bash
# 正常开发...
git add .
git commit -m "feat(akshare): 集成AKShare获取真实行情"

# 持续提交，使用Conventional Commits规范
git commit -m "feat(redis): 实现Redis缓存热点数据"
git commit -m "refactor: 重构ResearcherAgent支持真实数据"
git commit -m "test: 添加数据服务单元测试"
```

#### 迭代完成，合并回master
```bash
# 切换回master
git checkout master

# 合并功能分支（建议使用--no-ff保留分支历史）
git merge --no-ff feature/iteration2-data -m "chore: 完成迭代2 - 数据增强版"

# 推送到GitHub
git push origin master
```

---

## 📝 Commit规范（Conventional Commits）

### 类型前缀
| 类型 | 用途 | 示例 |
|------|------|------|
| `feat` | 新增功能 | `feat: 添加舆情分析Agent` |
| `fix` | 修复bug | `fix: 修复K线图渲染问题` |
| `docs` | 文档更新 | `docs: 更新SPEC.md添加I2详细设计` |
| `style` | 代码格式调整 | `style: 统一Java代码格式` |
| `refactor` | 重构 | `refactor: 重构MainOrchestrator` |
| `test` | 测试 | `test: 添加技术指标测试` |
| `chore` | 构建/工具 | `chore: 更新依赖版本` |

### 示例commit消息
```bash
# 迭代1的commit（已完成）
git commit -m "feat(Iteration1): MVP基础版 - 完整的4个Agent流水线"

# 迭代2开发中的commit
git commit -m "feat(akshare): 集成AKShare获取真实历史K线"
git commit -m "feat(redis): 实现Redis缓存层"
git commit -m "fix: 处理API限流问题"
git commit -m "docs: 更新README添加真实数据说明"

# 迭代完成的合并commit
git commit -m "chore: 完成迭代2 - 数据增强版"
```

---

## 🔄 每个迭代的完整Git流程

### 以迭代2（数据增强版）为例

#### 步骤1：创建分支
```bash
cd /Users/simon/trae_solo/stock_v2

# 确保当前在master且是最新的
git checkout master
git pull origin master  # （如果已连接GitHub）

# 创建I2分支
git checkout -b feature/iteration2-data
```

#### 步骤2：开发迭代
```bash
# 正常开发、提交
git add backend/pom.xml
git commit -m "feat: 添加AKShare依赖"

git add backend/src/main/java/...
git commit -m "feat(akshare): 实现真实行情数据服务"

git add backend/src/main/resources/application.yml
git commit -m "config: 配置Redis和数据源"
```

#### 步骤3：迭代完成
```bash
# 1. 确保所有文件已提交
git status

# 2. 切换回master
git checkout master

# 3. 合并功能分支
git merge --no-ff feature/iteration2-data -m "chore: 完成迭代2 - 数据增强版"

# 4. 推送到GitHub
git push origin master
```

#### 步骤4：准备下一个迭代
```bash
# 创建I3分支
git checkout -b feature/iteration3-ai master
```

---

## 📊 GitHub仓库设置

### 创建GitHub仓库步骤
1. 访问 https://github.com/new
2. 填写仓库信息：
   - Repository name: `stock-analysis-system`（或你喜欢的名字）
   - 选择 Public 或 Private
   - 不要初始化任何东西（README/.gitignore/LICENSE）
3. 点击 "Create repository"

### 关联本地仓库到GitHub
```bash
# 替换为你的GitHub用户名和仓库名
git remote add origin https://github.com/<你的用户名>/<仓库名>.git

# 验证远程仓库
git remote -v

# 推送master分支（I1）
git push -u origin master
```

### 后续迭代推送
```bash
# 完成迭代后推送master
git push origin master
```

---

## 📂 分支保护建议（可选）

### master分支保护（生产分支）
- 禁止直接push到master（可选）
- 需要PR审查合并
- CI通过后才能合并

### 功能分支命名规范
```
feature/iteration{数字}-{简短描述}
例如:
feature/iteration2-data
feature/iteration3-ai
feature/iteration4-sentiment
feature/iteration5-debate
feature/iteration6-vector
feature/iteration7-production
```

---

## 🎯 快速参考命令

### 查看当前分支状态
```bash
git status
git branch
git log --oneline --graph --all
```

### 创建分支
```bash
git checkout -b <分支名> <从哪个分支>
```

### 切换分支
```bash
git checkout <分支名>
```

### 合并分支
```bash
git checkout <目标分支>
git merge --no-ff <源分支> -m "commit消息"
```

---

## ✅ 当前状态总结

✅ **已完成**：
- Git仓库初始化
- 初始commit（I1: MVP基础版）
- .gitignore配置
- 分支策略文档

📋 **下一步**：
1. 在GitHub上创建新仓库
2. 运行关联远程仓库命令
3. 开始迭代2开发！

---

*Git工作流文档创建于 2026-05-16*
