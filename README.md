# 股票智能分析系统 - MVP版本

## 项目简介

这是一个基于多Agent架构的智能股票分析系统，采用MVP（最小可行产品）设计。

## 技术栈

### 后端
- Java 21
- Spring Boot 3.2.4
- H2 Database（内存数据库）
- Apache PDFBox（PDF生成）
- Lombok

### 前端
- Vue 3
- Element Plus
- ECharts
- Axios
- Vite

## 功能特性

- 4个Agent流水线编排（研究员、技术分析师、舆情分析师、投资经理）
- 真实技术指标计算（MACD/RSI）
- K线图可视化
- SSE实时进度推送
- 自选股管理
- PDF报告导出

## 快速启动

### 方式1：Maven + npm

```bash
# 启动后端
cd backend
mvn spring-boot:run

# 新开终端，启动前端
cd frontend
npm install
npm run dev
```

### 方式2：Gradle + npm

```bash
# 启动后端
cd backend
./gradlew bootRun

# 新开终端，启动前端
cd frontend
npm install
npm run dev
```

## 访问地址

- 前端界面：http://localhost:3000
- 后端API：http://localhost:8080
- H2控制台：http://localhost:8080/h2-console

## API接口

### 股票分析
- POST /api/analysis - 同步分析
- GET /api/analysis/stream/{stockCode} - SSE流式分析
- GET /api/analysis/pdf/{stockCode} - 下载PDF报告

### 自选股管理
- GET /api/watchlist - 获取自选股列表
- POST /api/watchlist - 添加自选股
- DELETE /api/watchlist/{id} - 删除自选股

## 快速使用

1. 在前端界面输入股票代码（如：600519）
2. 点击"开始分析"按钮
3. 查看分析结果和K线图
4. 点击"下载PDF报告"保存分析结果

## 项目结构

```
stock_v2/
├── backend/                    # 后端 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/stock/analysis/
│   │       ├── controller/    # 控制器层
│   │       ├── service/       # 服务层
│   │       ├── model/         # 数据模型
│   │       └── repository/    # 数据访问层
│   ├── src/main/resources/
│   │   └── application.yml    # 配置文件
│   └── pom.xml               # Maven配置
│
└── frontend/                  # 前端 (Vue 3)
    ├── src/
    │   ├── components/        # 组件
    │   ├── App.vue           # 主组件
    │   └── main.js           # 入口文件
    ├── index.html
    └── package.json          # npm配置
```
