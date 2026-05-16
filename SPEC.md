# 多Agent智能化股市分析系统 - 完整规格说明书

## 📋 文档概述

本文档详细描述了多Agent智能化股市分析系统的完整架构和分阶段实施计划。系统采用**迭代式开发**，每个迭代都是一个可交付、可运行、可验证的完整系统。

---

## 🎯 迭代路线图总览

### 迭代周期概览

| 迭代 | 名称 | 周期 | 核心目标 | 关键技术 | 可验证成果 |
|------|------|------|----------|----------|------------|
| **I1** | MVP基础版 | 1-2天 | 核心框架验证 | Spring Boot + Mock数据 | ✅ **已完成** |
| **I2** | 数据增强版 | 2-3天 | 真实行情接入 | AKShare + H2优化 | 真实K线数据 |
| **I3** | AI初探版 | 3-4天 | LLM基础集成 | SpringAI + 智谱AI | AI驱动的分析 |
| **I4** | 舆情增强版 | 3-4天 | 舆情深度分析 | 财经新闻API + 情感分析 | 真实舆情数据 |
| **I5** | 多模型辩论版 | 4-5天 | 多模型协作 | 3个LLM + 仲裁机制 | AI辩论决策 |
| **I6** | 向量检索版 | 3-4天 | 历史分析复用 | PostgreSQL + pgvector | 相似分析推荐 |
| **I7** | 生产就绪版 | 4-5天 | 生产环境优化 | Docker + Redis + 监控 | 可部署系统 |

**总计预估**：20-27个工作日

---

## 🏗️ 迭代1：MVP基础版 ✅ 已完成

### 1.1 迭代目标
快速构建可运行的全流程闭环原型，验证核心设计思路。

### 1.2 已实现功能
- ✅ 4个Agent流水线编排（研究员→技术分析师→舆情分析师→投资经理）
- ✅ 真实技术指标计算（MACD/RSI）
- ✅ K线图可视化（ECharts）
- ✅ SSE实时进度推送
- ✅ 自选股管理（H2数据库）
- ✅ PDF报告导出

### 1.3 技术栈
- 后端：Java 21 + Spring Boot 3.2+
- 存储：H2数据库（内存）
- 前端：Vue 3 + Element Plus + ECharts
- 部署：本地直接运行

### 1.4 核心文件结构
```
stock_v2/
├── backend/
│   ├── src/main/java/com/stock/analysis/
│   │   ├── controller/
│   │   │   ├── StockAnalysisController.java
│   │   │   └── WatchlistController.java
│   │   ├── service/
│   │   │   ├── orchestrator/
│   │   │   │   └── MainOrchestrator.java
│   │   │   ├── agent/
│   │   │   │   ├── ResearcherAgent.java (Mock数据)
│   │   │   │   ├── TechAnalystAgent.java
│   │   │   │   ├── SentimentAgent.java (Mock舆情)
│   │   │   │   └── InvestmentManagerAgent.java
│   │   │   ├── indicator/
│   │   │   │   └── TechnicalIndicatorService.java
│   │   │   └── report/
│   │   │       └── PDFReportService.java
│   │   └── model/
│   │       └── dto/ (8个DTO类)
│   └── pom.xml
└── frontend/
    ├── src/
    │   ├── App.vue
    │   └── components/
    │       └── KLineChart.vue
    └── package.json
```

### 1.5 API接口
```yaml
POST /api/analysis              # 同步股票分析
GET /api/analysis/stream/{code} # SSE流式分析
GET /api/analysis/pdf/{code}    # 下载PDF报告
GET /api/watchlist              # 获取自选股
POST /api/watchlist             # 添加自选股
DELETE /api/watchlist/{id}     # 删除自选股
```

### 1.6 验证标准
- [ ] 后端启动正常（端口8080）
- [ ] 前端启动正常（端口3000）
- [ ] 输入股票代码600519，收到分析结果
- [ ] K线图正确渲染
- [ ] PDF报告可下载

### 1.7 已知限制
- ❌ 使用Mock数据（非真实行情）
- ❌ 舆情为预设Demo数据
- ❌ 无AI能力（全规则引擎）

---

## 🚀 迭代2：数据增强版

### 2.1 迭代目标
接入真实股票行情数据，提升数据质量。

### 2.2 新增功能
- 📊 **真实K线数据**：集成AKShare获取真实历史K线
- 📈 **实时行情**：获取当前股价、涨跌幅、成交量
- 💾 **数据库升级**：H2 → MySQL/PostgreSQL，支持数据持久化
- 🔄 **数据缓存**：添加Redis缓存热点数据

### 2.3 技术架构

#### 2.3.1 数据服务层
```java
// MarketDataService.java
public interface MarketDataService {
    // 获取历史K线（AKShare）
    List<KLine> getHistoricalKLine(String stockCode, LocalDate startDate, LocalDate endDate);
    
    // 获取实时行情
    RealTimeQuote getRealTimeQuote(String stockCode);
    
    // 批量获取行情（用于自选股）
    List<RealTimeQuote> batchGetQuotes(List<String> stockCodes);
}

// 实现：AKShareMarketDataService.java
@Service
public class AKShareMarketDataService implements MarketDataService {
    @Autowired
    private AKShareClient akshareClient;
    
    @Override
    public List<KLine> getHistoricalKLine(String stockCode, LocalDate start, LocalDate end) {
        // 调用AKShare API
        String data = akshareClient.getKLine(stockCode, start, end);
        return parseKLineData(data);
    }
}
```

#### 2.3.2 缓存策略
```
Redis缓存设计：
┌─────────────────────────────────────────────┐
│ Key: quote:{stockCode}                      │
│ Type: Hash                                   │
│ TTL: 60s (1分钟)                             │
│ Fields: {                                    │
│   price: "1850.00",                          │
│   change: "25.00",                           │
│   changePercent: "1.37",                     │
│   volume: "123456",                          │
│   timestamp: "1705312200"                    │
│ }                                           │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Key: kline:{stockCode}:{startDate}:{endDate} │
│ Type: String (JSON)                          │
│ TTL: 3600s (1小时)                           │
│ Content: [KLine, KLine, ...]                │
└─────────────────────────────────────────────┘
```

### 2.4 数据库设计（MySQL）

#### 2.4.1 股票基础信息表
```sql
CREATE TABLE stock_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(10) NOT NULL UNIQUE,
    stock_name VARCHAR(100) NOT NULL,
    industry VARCHAR(50),
    market VARCHAR(20),  -- SH/SZ
    total_shares DECIMAL(20, 2),
    float_shares DECIMAL(20, 2),
    listing_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_code ON stock_info(stock_code);
CREATE INDEX idx_industry ON stock_info(industry);
```

#### 2.4.2 K线数据表
```sql
CREATE TABLE kline_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(10) NOT NULL,
    trade_date DATE NOT NULL,
    open_price DECIMAL(10, 2),
    high_price DECIMAL(10, 2),
    low_price DECIMAL(10, 2),
    close_price DECIMAL(10, 2),
    volume BIGINT,
    turnover DECIMAL(20, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stock_date (stock_code, trade_date)
);

CREATE INDEX idx_trade_date ON kline_data(trade_date);
CREATE INDEX idx_stock_date ON kline_data(stock_code, trade_date);
```

#### 2.4.3 实时行情表
```sql
CREATE TABLE realtime_quote (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(10) NOT NULL UNIQUE,
    current_price DECIMAL(10, 2),
    change_amount DECIMAL(10, 2),
    change_percent DECIMAL(5, 2),
    volume BIGINT,
    turnover DECIMAL(20, 2),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_update_time ON realtime_quote(update_time);
```

### 2.5 ResearcherAgent重构
```java
@Service
public class ResearcherAgent {
    
    private final MarketDataService marketDataService;
    private final StockInfoRepository stockInfoRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public ResearcherResult analyze(String stockCode) {
        // 1. 尝试从缓存获取
        String cacheKey = "kline:" + stockCode + ":latest";
        List<KLine> kLines = (List<KLine>) redisTemplate.opsForValue().get(cacheKey);
        
        if (kLines == null) {
            // 2. 从AKShare获取真实数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);
            kLines = marketDataService.getHistoricalKLine(stockCode, startDate, endDate);
            
            // 3. 存入缓存（1小时）
            redisTemplate.opsForValue().set(cacheKey, kLines, 1, TimeUnit.HOURS);
            
            // 4. 持久化到数据库
            saveKLineData(stockCode, kLines);
        }
        
        // 5. 获取实时行情
        RealTimeQuote quote = marketDataService.getRealTimeQuote(stockCode);
        
        return ResearcherResult.builder()
            .stockCode(stockCode)
            .stockName(getStockName(stockCode))
            .kLines(kLines)
            .realTimeQuote(quote)
            .build();
    }
}
```

### 2.6 前端增强
```vue
<!-- 实时行情展示 -->
<template>
  <div class="stock-header">
    <h2>{{ stockCode }} - {{ stockName }}</h2>
    <div class="quote-info">
      <span class="price">{{ currentPrice }}</span>
      <span class="change" :class="changeClass">
        {{ changeAmount }} ({{ changePercent }}%)
      </span>
    </div>
    <div class="update-time">更新时间: {{ updateTime }}</div>
  </div>
</template>

<script setup>
const updateQuote = async () => {
  const quote = await axios.get(`/api/quote/${stockCode.value}`)
  currentPrice.value = quote.data.price
  changeAmount.value = quote.data.change
  changePercent.value = quote.data.changePercent
  updateTime.value = new Date(quote.data.timestamp)
}
</script>
```

### 2.7 新增API
```yaml
GET /api/quote/{stockCode}           # 获取实时行情
GET /api/quote/batch?codes=600519,000858  # 批量获取行情
GET /api/stock/info/{stockCode}      # 获取股票基本信息
POST /api/quote/refresh              # 刷新行情缓存
```

### 2.8 依赖项
```xml
<!-- AKShare Java客户端 -->
<dependency>
    <groupId>com.github.AKShare</groupId>
    <artifactId>akshare</artifactId>
    <version>1.12.0</version>
</dependency>

<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

### 2.9 验证标准
- [ ] 输入股票代码600519，获取真实K线数据（验证是否为真实历史数据）
- [ ] K线图显示真实价格走势
- [ ] 实时行情每分钟自动刷新
- [ ] 行情数据缓存正常工作（相同请求第二次更快）
- [ ] 数据库持久化成功（重启后数据不丢失）

### 2.10 风险与应对
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| AKShare API不稳定 | 中 | 中 | 添加重试机制 + Mock降级 |
| Redis连接失败 | 低 | 中 | 使用本地缓存作为备份 |
| 数据解析错误 | 中 | 高 | 添加数据校验 + 异常日志 |

### 2.11 预估工时
- AKShare集成：1天
- Redis缓存：0.5天
- MySQL持久化：0.5天
- 前端增强：0.5天
- 测试与修复：1天
- **总计：3.5天**

---

## 🤖 迭代3：AI初探版

### 3.1 迭代目标
集成LLM能力，让AI参与股票分析，替代部分规则引擎。

### 3.2 新增功能
- 🧠 **SpringAI集成**：接入智谱AI（GLM-4）
- 📝 **AI基本面分析**：使用LLM分析公司基本面
- 💬 **AI投资建议**：基于技术+舆情生成投资建议
- ⚙️ **提示词模板**：结构化Prompt设计

### 3.3 技术架构

#### 3.3.1 LLM网关设计
```java
// LLM网关接口
public interface LLMGateway {
    // 同步查询
    String query(String prompt);
    
    // 流式查询（SSE）
    Flux<String> queryStream(String prompt);
    
    // 模型切换
    void switchModel(String modelType);
    
    // 获取可用模型列表
    List<String> getAvailableModels();
}

// 智谱AI实现
@Service
public class ZhipuLLMGateway implements LLMGateway {
    
    private final RestTemplate restTemplate;
    private final String apiKey;
    private volatile String currentModel = "glm-4";
    
    @Override
    public String query(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        Map<String, Object> body = Map.of(
            "model", currentModel,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://open.bigmodel.cn/api/paas/v4/chat/completions",
            request,
            Map.class
        );
        
        return extractContent(response.getBody());
    }
}
```

#### 3.3.2 提示词模板
```java
// Prompts.java
public class Prompts {
    
    // 基本面分析提示词
    public static String fundamentalAnalysisPrompt(StockInfo info, List<KLine> kLines) {
        return String.format("""
            你是一位资深金融分析师。请分析以下股票的基本面：
            
            股票代码：%s
            股票名称：%s
            所属行业：%s
            
            最近60天K线数据统计：
            - 平均收盘价：%.2f
            - 最高价：%.2f
            - 最低价：%.2f
            - 涨跌幅：%.2f%%
            - 平均成交量：%.2f
            
            请从以下角度进行分析：
            1. 公司业务概况（如果知道）
            2. 股价走势分析
            3. 成交量变化趋势
            4. 主要风险点
            5. 基本面评分（1-10分）
            
            请用JSON格式返回，包含字段：analysis, riskPoints, score
            """, 
            info.getStockCode(),
            info.getStockName(),
            info.getIndustry(),
            calculateAvgClose(kLines),
            calculateMaxHigh(kLines),
            calculateMinLow(kLines),
            calculateChangePercent(kLines),
            calculateAvgVolume(kLines)
        );
    }
    
    // 投资建议提示词
    public static String investmentAdvicePrompt(
        TechResult tech, 
        SentimentResult sentiment, 
        FundamentalResult fundamental
    ) {
        return String.format("""
            作为一位专业投资顾问，请结合以下分析结果给出投资建议：
            
            【技术面分析】
            - RSI指标：%.2f
            - 技术建议：%s
            
            【舆情分析】
            - 整体舆情：%s
            - 新闻数量：%d条
            
            【基本面】
            - 综合评分：%.1f/10
            
            请给出：
            1. 投资建议（买入/持有/观望）
            2. 目标价格区间
            3. 止损价格
            4. 风险等级（低/中/高）
            5. 核心投资逻辑（3句话）
            
            请用JSON格式返回：
            {
                "recommendation": "...",
                "targetPrice": "...",
                "stopLoss": "...",
                "riskLevel": "...",
                "reasoning": ["...", "...", "..."]
            }
            """,
            tech.getRsi().getRsi(),
            tech.getRecommendation(),
            sentiment.getOverallSentiment(),
            sentiment.getNews().size(),
            fundamental.getScore()
        );
    }
}
```

### 3.4 InvestmentManagerAgent重构
```java
@Service
public class InvestmentManagerAgent {
    
    private final LLMGateway llmGateway;
    private final Prompts prompts;
    
    public InvestmentResult decide(
        TechResult tech, 
        SentimentResult sentiment,
        FundamentalResult fundamental
    ) {
        // 1. 使用规则计算基础分数
        double ruleScore = calculateRuleScore(tech, sentiment);
        
        // 2. 调用AI生成建议
        String prompt = prompts.investmentAdvicePrompt(tech, sentiment, fundamental);
        String aiResponse = llmGateway.query(prompt);
        
        // 3. 解析AI响应
        InvestmentResult aiResult = parseAIResponse(aiResponse);
        
        // 4. 综合规则和AI结果
        String finalRecommendation = blendRecommendation(ruleScore, aiResult.getRecommendation());
        String finalRiskLevel = blendRiskLevel(ruleScore, aiResult.getRiskLevel());
        
        return InvestmentResult.builder()
            .recommendation(finalRecommendation)
            .riskLevel(finalRiskLevel)
            .targetPrice(aiResult.getTargetPrice())
            .stopLossPrice(aiResult.getStopLoss())
            .reason(String.join("; ", aiResult.getReasoning()))
            .aiGenerated(true)
            .confidence(calculateConfidence(ruleScore, aiResult))
            .build();
    }
    
    private double calculateRuleScore(TechResult tech, SentimentResult sentiment) {
        double techScore = (100 - tech.getRsi().getRsi()) / 100;
        double sentimentScore = "积极".equals(sentiment.getOverallSentiment()) ? 0.8 : 0.5;
        return (techScore + sentimentScore) / 2;
    }
    
    private InvestmentResult parseAIResponse(String response) {
        // JSON解析逻辑
        JSONObject json = JSON.parseObject(response);
        return InvestmentResult.builder()
            .recommendation(json.getString("recommendation"))
            .targetPrice(json.getString("targetPrice"))
            .stopLoss(json.getString("stopLoss"))
            .riskLevel(json.getString("riskLevel"))
            .reasoning(Arrays.asList(json.getJSONArray("reasoning")))
            .build();
    }
}
```

### 3.5 API增强
```yaml
POST /api/analysis/ai                 # AI增强分析
GET /api/llm/models                   # 获取可用模型
POST /api/llm/switch                   # 切换模型
GET /api/analysis/history              # 分析历史
```

### 3.6 配置管理
```yaml
# application.yml
spring:
  ai:
    zhipu:
      api-key: ${ZHIPU_API_KEY}
      model: glm-4
      temperature: 0.7
      max-tokens: 1000
      timeout: 30000
```

### 3.7 验证标准
- [ ] LLM成功调用（检查日志中的API调用）
- [ ] AI生成的投资建议与规则引擎结果一致或更优
- [ ] 错误处理正常（API失败时优雅降级）
- [ ] 响应时间可接受（<5秒）

### 3.8 风险与应对
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| LLM API调用失败 | 中 | 高 | 规则引擎降级 + 重试3次 |
| API响应慢 | 高 | 中 | 超时设置30s + 异步处理 |
| Token超限 | 低 | 中 | 提示词压缩 + 缓存结果 |
| API Key泄露 | 低 | 极高 | 使用环境变量 + 审计日志 |

### 3.9 预估工时
- SpringAI集成：1天
- 提示词设计：0.5天
- Agent重构：1天
- 错误处理：0.5天
- 测试与调优：1天
- **总计：4天**

---

## 📰 迭代4：舆情增强版

### 4.1 迭代目标
构建完整的舆情分析系统，获取真实财经新闻并进行情感分析。

### 4.2 新增功能
- 📰 **新闻抓取**：集成东方财富/同花顺财经API
- 🎭 **情感分析**：使用LLM进行新闻情感评分
- 🔥 **热点识别**：识别近期热点事件
- 📊 **舆情报告**：生成舆情分析报告

### 4.3 技术架构

#### 4.3.1 舆情服务
```java
// SentimentDataService.java
public interface SentimentDataService {
    // 获取财经新闻
    List<NewsArticle> fetchFinancialNews(String stockCode);
    
    // 获取公告
    List<Announcement> fetchAnnouncements(String stockCode);
    
    // 批量获取
    List<SentimentSource> batchFetch(String stockCode);
}

// 东方财富实现
@Service
public class EastMoneySentimentService implements SentimentDataService {
    
    @Override
    public List<NewsArticle> fetchFinancialNews(String stockCode) {
        String url = String.format(
            "https://np-anotice-stock.eastmoney.com/api/security/ann?sr=-1&page_size=10&page_index=1&ann_type=SHA,CYB,SZA"
        );
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        return parseNewsResponse(response.getBody());
    }
}
```

#### 4.3.2 情感分析服务
```java
// SentimentAnalysisService.java
public interface SentimentAnalysisService {
    // 分析单条新闻情感
    SentimentScore analyzeNewsSentiment(NewsArticle article);
    
    // 分析社交媒体情感
    SentimentScore analyzeSocialSentiment(SocialPost post);
    
    // 计算总体情感
    OverallSentiment calculateOverallSentiment(List<SentimentScore> scores);
}

// LLM情感分析
@Service
public class LLMSentimentAnalysis implements SentimentAnalysisService {
    
    private final LLMGateway llmGateway;
    
    @Override
    public SentimentScore analyzeNewsSentiment(NewsArticle article) {
        String prompt = String.format("""
            分析以下财经新闻的情感倾向：
            
            标题：%s
            内容：%s
            来源：%s
            时间：%s
            
            请判断：
            1. 情感倾向（积极/消极/中性）
            2. 情感强度（0-100）
            3. 关键信息点
            
            请用JSON格式返回：
            {
                "sentiment": "positive/negative/neutral",
                "intensity": 0-100,
                "keyPoints": ["...", "..."]
            }
            """,
            article.getTitle(),
            article.getContent(),
            article.getSource(),
            article.getPublishTime()
        );
        
        String response = llmGateway.query(prompt);
        return parseSentimentResponse(response);
    }
    
    @Override
    public OverallSentiment calculateOverallSentiment(List<SentimentScore> scores) {
        double avgIntensity = scores.stream()
            .mapToDouble(SentimentScore::getIntensity)
            .average()
            .orElse(50);
        
        long positiveCount = scores.stream()
            .filter(s -> "positive".equals(s.getSentiment()))
            .count();
        
        String overallSentiment;
        if (positiveCount > scores.size() * 0.6) {
            overallSentiment = "积极";
        } else if (positiveCount < scores.size() * 0.4) {
            overallSentiment = "消极";
        } else {
            overallSentiment = "中性";
        }
        
        return OverallSentiment.builder()
            .sentiment(overallSentiment)
            .intensity(avgIntensity)
            .positiveRatio(positiveCount * 1.0 / scores.size())
            .totalNews(scores.size())
            .build();
    }
}
```

#### 4.3.3 SentimentAgent重构
```java
@Service
public class SentimentAgent {
    
    private final SentimentDataService sentimentDataService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public SentimentResult analyze(String stockCode) {
        // 1. 获取财经新闻
        List<NewsArticle> newsList = getCachedOrFetch(
            "news:" + stockCode,
            () -> sentimentDataService.fetchFinancialNews(stockCode),
            30, TimeUnit.MINUTES
        );
        
        // 2. 获取公告
        List<Announcement> announcements = sentimentDataService.fetchAnnouncements(stockCode);
        
        // 3. 情感分析
        List<SentimentScore> sentimentScores = newsList.stream()
            .map(sentimentAnalysisService::analyzeNewsSentiment)
            .collect(Collectors.toList());
        
        // 4. 计算总体情感
        OverallSentiment overall = sentimentAnalysisService.calculateOverallSentiment(sentimentScores);
        
        // 5. 识别热点事件
        List<HotEvent> hotEvents = identifyHotEvents(newsList, sentimentScores);
        
        // 6. 生成舆情建议
        String recommendation = generateRecommendation(overall, hotEvents);
        
        return SentimentResult.builder()
            .news(newsList)
            .sentimentScores(sentimentScores)
            .overallSentiment(overall.getSentiment())
            .sentimentIntensity(overall.getIntensity())
            .hotEvents(hotEvents)
            .recommendation(recommendation)
            .build();
    }
    
    private List<HotEvent> identifyHotEvents(
        List<NewsArticle> news, 
        List<SentimentScore> scores
    ) {
        // 基于关键词和时间密度识别热点
        return news.stream()
            .filter(n -> scores.stream()
                .anyMatch(s -> s.getIntensity() > 70))
            .map(n -> HotEvent.builder()
                .title(n.getTitle())
                .sentiment(scores.stream()
                    .findFirst()
                    .map(SentimentScore::getSentiment)
                    .orElse("neutral"))
                .keyPoints(n.getKeyPoints())
                .build())
            .collect(Collectors.toList());
    }
}
```

### 4.4 数据模型
```java
// NewsArticle.java
@Data
@Builder
public class NewsArticle {
    private String title;
    private String content;
    private String source;
    private LocalDateTime publishTime;
    private String url;
    private List<String> tags;
}

// SentimentScore.java
@Data
@Builder
public class SentimentScore {
    private String sentiment;      // positive/negative/neutral
    private double intensity;       // 0-100
    private List<String> keyPoints;
}

// HotEvent.java
@Data
@Builder
public class HotEvent {
    private String title;
    private String sentiment;
    private List<String> keyPoints;
    private LocalDateTime detectedTime;
}
```

### 4.5 新增API
```yaml
GET /api/sentiment/news/{stockCode}      # 获取新闻
GET /api/sentiment/announcements/{code}   # 获取公告
GET /api/sentiment/hot-events/{code}     # 获取热点事件
POST /api/sentiment/analyze              # 分析舆情
```

### 4.6 前端舆情展示
```vue
<template>
  <el-card>
    <template #header>
      <span>舆情分析</span>
      <el-tag :type="sentimentTagType" size="small">
        {{ overallSentiment }}
      </el-tag>
    </template>
    
    <div class="sentiment-summary">
      <div class="sentiment-meter">
        <el-progress 
          :percentage="sentimentIntensity" 
          :color="sentimentColor"
        />
      </div>
      <div class="sentiment-detail">
        <span>情感强度：{{ sentimentIntensity }}%</span>
      </div>
    </div>
    
    <el-divider />
    
    <div class="hot-events">
      <h4>🔥 热点事件</h4>
      <el-timeline>
        <el-timeline-item
          v-for="event in hotEvents"
          :key="event.title"
          :type="event.type"
        >
          <h5>{{ event.title }}</h5>
          <p>{{ event.keyPoints.join(', ') }}</p>
        </el-timeline-item>
      </el-timeline>
    </div>
    
    <el-divider />
    
    <div class="news-list">
      <h4>📰 最新新闻</h4>
      <el-collapse>
        <el-collapse-item
          v-for="news in newsList"
          :key="news.title"
          :title="news.title"
        >
          <p>{{ news.content }}</p>
          <div class="news-meta">
            <span>来源：{{ news.source }}</span>
            <span>时间：{{ news.publishTime }}</span>
            <el-tag :type="getSentimentTag(news.sentiment)" size="small">
              {{ news.sentiment }}
            </el-tag>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  sentimentData: Object
})

const newsList = computed(() => props.sentimentData?.news || [])
const overallSentiment = computed(() => props.sentimentData?.overallSentiment || '中性')
const sentimentIntensity = computed(() => props.sentimentData?.sentimentIntensity || 50)
const hotEvents = computed(() => props.sentimentData?.hotEvents || [])

const sentimentTagType = computed(() => {
  if (overallSentiment.value === '积极') return 'success'
  if (overallSentiment.value === '消极') return 'danger'
  return 'info'
})

const sentimentColor = computed(() => {
  if (sentimentIntensity.value > 70) return '#67C23A'
  if (sentimentIntensity.value < 30) return '#F56C6C'
  return '#E6A23C'
})
</script>
```

### 4.7 验证标准
- [ ] 获取至少5条真实财经新闻
- [ ] 情感分析结果合理（积极新闻判为positive）
- [ ] 热点事件识别准确
- [ ] 舆情数据缓存正常工作
- [ ] 异常处理正常（API失败时有降级）

### 4.8 风险与应对
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 新闻API不稳定 | 中 | 中 | 多源备份 + Mock数据降级 |
| 情感分析不准确 | 高 | 中 | 阈值调整 + 人工标注样本 |
| 数据量大导致响应慢 | 中 | 中 | 分页加载 + 懒加载 |
| 敏感信息泄露 | 低 | 高 | 内容过滤 + 脱敏处理 |

### 4.9 预估工时
- 新闻API集成：1天
- 情感分析服务：1天
- 热点事件识别：0.5天
- Agent重构：0.5天
- 前端展示：0.5天
- 测试与优化：1天
- **总计：4.5天**

---

## 🗣️ 迭代5：多模型辩论版

### 5.1 迭代目标
实现多LLM模型协作辩论机制，通过多角度分析消除单模型偏见。

### 5.2 新增功能
- 🔄 **多模型切换**：支持智谱/通义/混元等多个LLM
- 💭 **观点辩论**：多模型从不同角度分析并交叉验证
- ⚖️ **仲裁机制**：综合多模型意见生成最终结论
- 📊 **置信度评估**：量化分析结论的可靠性

### 5.3 技术架构

#### 5.3.1 多模型网关
```java
// MultiModelGateway.java
public interface MultiModelGateway {
    // 多模型并行查询
    List<LLMResponse> queryMultiple(List<String> modelTypes, String prompt);
    
    // 流式多模型查询
    Flux<String> queryMultipleStream(List<String> modelTypes, String prompt);
}

// 多模型实现
@Service
public class MultiModelGatewayImpl implements MultiModelGateway {
    
    private final Map<String, LLMClient> modelClients;
    private final ExecutorService executor;
    
    public MultiModelGatewayImpl() {
        this.modelClients = Map.of(
            "zhipu", new ZhipuClient(),
            "tongyi", new TongyiClient(),
            "hunyuan", new HunyuanClient()
        );
        this.executor = Executors.newFixedThreadPool(3);
    }
    
    @Override
    public List<LLMResponse> queryMultiple(List<String> modelTypes, String prompt) {
        List<CompletableFuture<LLMResponse>> futures = modelTypes.stream()
            .filter(modelClients::containsKey)
            .map(type -> CompletableFuture.supplyAsync(
                () -> modelClients.get(type).query(prompt),
                executor
            ))
            .collect(Collectors.toList());
        
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
}
```

#### 5.3.2 辩论控制器
```java
// DebateController.java
public interface DebateController {
    DebateResult runDebate(DebateContext context);
}

// 辩论实现
@Service
public class DebateControllerImpl implements DebateController {
    
    private final MultiModelGateway multiModelGateway;
    
    private static final int MAX_ROUNDS = 2;
    
    @Override
    public DebateResult runDebate(DebateContext context) {
        List<LLMOpinion> opinions = new ArrayList<>();
        
        // 第1轮：独立分析
        opinions.addAll(generateInitialOpinions(context));
        
        // 第2轮：交叉验证（可选）
        if (!hasConsensus(opinions)) {
            opinions = crossValidate(opinions, context);
        }
        
        // 仲裁裁决
        ArbitrationResult arbitration = adjudicate(opinions);
        
        return DebateResult.builder()
            .opinions(opinions)
            .consensus(arbitration.getConsensus())
            .confidence(arbitration.getConfidence())
            .disagreements(arbitration.getDisagreements())
            .build();
    }
    
    private List<LLMOpinion> generateInitialOpinions(DebateContext context) {
        List<String> models = List.of("zhipu", "tongyi", "hunyuan");
        
        String prompt = buildMultiAnglePrompt(context);
        
        return multiModelGateway.queryMultiple(models, prompt)
            .stream()
            .map(this::parseOpinion)
            .collect(Collectors.toList());
    }
    
    private String buildMultiAnglePrompt(DebateContext context) {
        return String.format("""
            作为一位%s分析师，请从%s角度分析以下股票：
            
            技术指标：
            - RSI: %.2f (%s)
            - MACD: %s
            
            舆情分析：
            - 整体舆情: %s
            - 新闻数量: %d
            
            请给出：
            1. 投资建议
            2. 置信度（0-100）
            3. 关键支撑因素
            4. 主要风险点
            
            请用JSON格式返回。
            """,
            context.getAgentType(),
            context.getAngle(),
            context.getTech().getRsi().getRsi(),
            context.getTech().getRecommendation(),
            context.getTech().getMacdSignal(),
            context.getSentiment().getOverallSentiment(),
            context.getSentiment().getNews().size()
        );
    }
}
```

#### 5.3.3 仲裁裁决器
```java
// ArbitrationController.java
public interface ArbitrationController {
    ArbitrationResult adjudicate(List<LLMOpinion> opinions);
}

@Service
public class ArbitrationControllerImpl implements ArbitrationController {
    
    @Override
    public ArbitrationResult adjudicate(List<LLMOpinion> opinions) {
        // 1. 统计建议分布
        Map<String, Long> recommendationCounts = opinions.stream()
            .collect(Collectors.groupingBy(
                LLMOpinion::getRecommendation,
                Collectors.counting()
            ));
        
        // 2. 确定共识
        String consensus = recommendationCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("观望");
        
        // 3. 计算置信度
        double confidence = calculateConfidence(opinions, consensus);
        
        // 4. 识别分歧
        List<String> disagreements = identifyDisagreements(opinions);
        
        // 5. 融合关键因素
        List<String> keyFactors = fuseKeyFactors(opinions, consensus);
        
        return ArbitrationResult.builder()
            .consensus(consensus)
            .confidence(confidence)
            .disagreements(disagreements)
            .keyFactors(keyFactors)
            .votingResult(recommendationCounts)
            .build();
    }
    
    private double calculateConfidence(List<LLMOpinion> opinions, String consensus) {
        long consensusCount = opinions.stream()
            .filter(o -> consensus.equals(o.getRecommendation()))
            .count();
        
        double agreementRate = (double) consensusCount / opinions.size();
        
        double avgConfidence = opinions.stream()
            .filter(o -> consensus.equals(o.getRecommendation()))
            .mapToDouble(LLMOpinion::getConfidence)
            .average()
            .orElse(50);
        
        return (agreementRate * 0.6 + avgConfidence / 100 * 0.4) * 100;
    }
}
```

#### 5.3.4 辩论上下文
```java
@Data
@Builder
public class DebateContext {
    private String taskId;
    private String stockCode;
    private TechResult tech;
    private SentimentResult sentiment;
    private FundamentalResult fundamental;
    private String agentType;      // 技术分析师/投资经理
    private String angle;          // 视角：技术面/基本面/情绪面
}

// LLM观点
@Data
@Builder
public class LLMOpinion {
    private String modelType;
    private String recommendation;
    private double confidence;
    private List<String> supportingFactors;
    private List<String> riskFactors;
    private String reasoning;
}

// 辩论结果
@Data
@Builder
public class DebateResult {
    private List<LLMOpinion> opinions;
    private String consensus;
    private double confidence;
    private List<String> disagreements;
    private Map<String, Long> votingResult;
}

// 仲裁结果
@Data
@Builder
public class ArbitrationResult {
    private String consensus;
    private double confidence;
    private List<String> disagreements;
    private List<String> keyFactors;
    private Map<String, Long> votingResult;
}
```

### 5.4 InvestmentManagerAgent重构（辩论版）
```java
@Service
public class InvestmentManagerAgent {
    
    private final DebateController debateController;
    private final LLMGateway llmGateway;
    
    public InvestmentResult decide(
        TechResult tech, 
        SentimentResult sentiment,
        FundamentalResult fundamental
    ) {
        // 1. 规则引擎快速判断
        double ruleScore = calculateRuleScore(tech, sentiment);
        
        // 2. 构建辩论上下文
        DebateContext context = DebateContext.builder()
            .taskId(UUID.randomUUID().toString())
            .stockCode(tech.getStockCode())
            .tech(tech)
            .sentiment(sentiment)
            .fundamental(fundamental)
            .agentType("投资经理")
            .angle("综合分析")
            .build();
        
        // 3. 执行辩论
        DebateResult debate = debateController.runDebate(context);
        
        // 4. 综合规则和辩论结果
        String finalRecommendation = blendResults(ruleScore, debate);
        String finalRiskLevel = determineRiskLevel(debate);
        String finalReasoning = String.join("; ", debate.getKeyFactors());
        
        return InvestmentResult.builder()
            .recommendation(finalRecommendation)
            .riskLevel(finalRiskLevel)
            .confidence(debate.getConfidence())
            .reason(finalReasoning)
            .debateResult(debate)
            .aiGenerated(true)
            .build();
    }
    
    private String blendResults(double ruleScore, DebateResult debate) {
        // 加权平均：规则40% + 辩论60%
        double debateScore = "买入".equals(debate.getConsensus()) ? 0.8 : 
                            "持有".equals(debate.getConsensus()) ? 0.5 : 0.3;
        
        double finalScore = ruleScore * 0.4 + debateScore * 0.6;
        
        if (finalScore > 0.65) return "买入";
        if (finalScore > 0.45) return "持有";
        return "观望";
    }
}
```

### 5.5 前端辩论可视化
```vue
<template>
  <el-card>
    <template #header>
      <span>🤖 AI多模型辩论</span>
      <el-tag type="success">置信度: {{ confidence }}%</el-tag>
    </template>
    
    <div class="debate-status">
      <el-progress :percentage="confidence" :color="confidenceColor" />
    </div>
    
    <el-divider />
    
    <div class="model-opinions">
      <h4>💭 各模型观点</h4>
      <el-card
        v-for="opinion in opinions"
        :key="opinion.model"
        class="opinion-card"
      >
        <template #header>
          <div class="opinion-header">
            <span>{{ opinion.model }}</span>
            <el-tag :type="getRecommendationType(opinion.recommendation)">
              {{ opinion.recommendation }}
            </el-tag>
          </div>
        </template>
        
        <div class="opinion-content">
          <div class="confidence">
            置信度: <el-rate v-model="opinion.confidence" disabled />
          </div>
          
          <div class="factors">
            <h5>支撑因素:</h5>
            <ul>
              <li v-for="factor in opinion.supporting" :key="factor">{{ factor }}</li>
            </ul>
          </div>
          
          <div class="risks">
            <h5>风险因素:</h5>
            <ul>
              <li v-for="risk in opinion.risks" :key="risk">{{ risk }}</li>
            </ul>
          </div>
        </div>
      </el-card>
    </div>
    
    <el-divider />
    
    <div class="consensus">
      <h4>⚖️ 仲裁结论</h4>
      <el-alert
        :title="consensus"
        :type="getConsensusType"
        show-icon
      />
      
      <div v-if="disagreements.length > 0" class="disagreements">
        <h5>主要分歧:</h5>
        <el-tag
          v-for="dis in disagreements"
          :key="dis"
          type="warning"
          style="margin-right: 8px;"
        >
          {{ dis }}
        </el-tag>
      </div>
    </div>
    
    <div class="voting-result">
      <h5>投票结果:</h5>
      <el-progress
        v-for="(count, rec) in votingResult"
        :key="rec"
        :percentage="(count / opinions.length) * 100"
        :format="val => `${rec}: ${count}票`"
      />
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  debateResult: Object
})

const opinions = computed(() => props.debateResult?.opinions || [])
const consensus = computed(() => props.debateResult?.consensus || '待分析')
const confidence = computed(() => props.debateResult?.confidence || 0)
const disagreements = computed(() => props.debateResult?.disagreements || [])
const votingResult = computed(() => props.debateResult?.votingResult || {})

const confidenceColor = computed(() => {
  if (confidence.value > 70) return '#67C23A'
  if (confidence.value > 40) return '#E6A23C'
  return '#F56C6C'
})

const getRecommendationType = (rec) => {
  if (rec === '买入') return 'success'
  if (rec === '持有') return 'warning'
  return 'info'
}

const getConsensusType = computed(() => {
  if (consensus.value === '买入') return 'success'
  if (consensus.value === '观望') return 'warning'
  return 'info'
})
</script>
```

### 5.6 新增配置
```yaml
spring:
  llm:
    models:
      zhipu:
        enabled: true
        api-key: ${ZHIPU_API_KEY}
        model: glm-4
        weight: 0.4
      tongyi:
        enabled: true
        api-key: ${TONGYI_API_KEY}
        model: qwen-turbo
        weight: 0.3
      hunyuan:
        enabled: false
        api-key: ${HUNYUAN_API_KEY}
        model: hunyuan-pro
        weight: 0.3
    
    debate:
      max-rounds: 2
      timeout: 30000
      consensus-threshold: 0.6
```

### 5.7 验证标准
- [ ] 3个模型并行调用成功
- [ ] 辩论结果合理（多数一致）
- [ ] 置信度计算正确
- [ ] 分歧识别准确
- [ ] 降级机制正常（部分模型失败时）

### 5.8 风险与应对
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 部分模型API失败 | 中 | 高 | 熔断器降级 + 权重重新分配 |
| 辩论耗时过长 | 高 | 中 | 超时控制 + 异步处理 |
| 模型观点严重分歧 | 中 | 中 | 置信度降低 + 人工介入提示 |
| API成本过高 | 中 | 中 | 缓存结果 + 按需调用 |

### 5.9 预估工时
- 多模型网关：1天
- 辩论控制器：1.5天
- 仲裁裁决器：1天
- Agent集成：0.5天
- 前端可视化：0.5天
- 测试与优化：1.5天
- **总计：6天**

---

## 🔍 迭代6：向量检索版

### 6.1 迭代目标
引入向量数据库，实现历史分析的相似性检索和复用。

### 6.2 新增功能
- 🗄️ **分析结果持久化**：PostgreSQL存储完整分析记录
- 🔮 **向量嵌入**：生成分析结果的语义向量
- 🔎 **相似性检索**：基于向量查找相似历史分析
- 💡 **智能推荐**：基于历史表现推荐分析策略

### 6.3 技术架构

#### 6.3.1 PostgreSQL + pgvector配置
```sql
-- 启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 历史分析表
CREATE TABLE analysis_history (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL UNIQUE,
    stock_code VARCHAR(10) NOT NULL,
    stock_name VARCHAR(100),
    analysis_date DATE NOT NULL,
    
    -- 分析结果
    recommendation VARCHAR(20),
    risk_level VARCHAR(20),
    target_price DECIMAL(10, 2),
    confidence DECIMAL(5, 2),
    
    -- 技术指标
    rsi DECIMAL(5, 2),
    macd_signal VARCHAR(50),
    
    -- 舆情
    sentiment VARCHAR(20),
    sentiment_intensity DECIMAL(5, 2),
    news_count INT,
    
    -- 向量嵌入（1024维，这里简化）
    embedding vector(1024),
    
    -- 摘要文本（用于展示）
    summary TEXT,
    key_factors TEXT,
    risk_factors TEXT,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_stock_code ON analysis_history(stock_code);
CREATE INDEX idx_analysis_date ON analysis_history(analysis_date);
CREATE INDEX idx_recommendation ON analysis_history(recommendation);

-- 向量索引（HNSW）
CREATE INDEX idx_embedding ON analysis_history 
USING hnsw (embedding vector_cosine_ops);
```

#### 6.3.2 嵌入服务
```java
// EmbeddingService.java
public interface EmbeddingService {
    // 生成文本嵌入
    float[] generateEmbedding(String text);
    
    // 生成分析结果嵌入
    float[] generateAnalysisEmbedding(AnalysisResult result);
}

// LLM嵌入实现
@Service
public class LLMEmbeddingService implements EmbeddingService {
    
    private final RestTemplate restTemplate;
    private final String apiKey;
    
    @Override
    public float[] generateEmbedding(String text) {
        String url = "https://open.bigmodel.cn/api/paas/v4/embeddings";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        Map<String, Object> body = Map.of(
            "model", "embedding-2",
            "input", text
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        
        List<Float> embedding = (List<Float>) ((Map) response.getBody()
            .get("data"))
            .get("embedding");
        
        return toFloatArray(embedding);
    }
    
    @Override
    public float[] generateAnalysisEmbedding(AnalysisResult result) {
        String text = String.format("""
            股票分析：
            股票代码：%s
            技术分析：RSI %.2f，%s
            舆情分析：%s，%d条新闻
            投资建议：%s，风险%s
            """,
            result.getStockCode(),
            result.getTechAnalyst().getRsi().getRsi(),
            result.getTechAnalyst().getRecommendation(),
            result.getSentiment().getOverallSentiment(),
            result.getSentiment().getNews().size(),
            result.getInvestment().getRecommendation(),
            result.getInvestment().getRiskLevel()
        );
        
        return generateEmbedding(text);
    }
}
```

#### 6.3.3 相似性检索服务
```java
// SimilaritySearchService.java
public interface SimilaritySearchService {
    // 查找相似分析
    List<SimilarAnalysis> findSimilar(AnalysisResult current, int topN);
    
    // 查找同一股票的历史分析
    List<AnalysisHistory> findHistory(String stockCode, int limit);
}

// 向量检索实现
@Service
public class VectorSearchService implements SimilaritySearchService {
    
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    
    @Override
    public List<SimilarAnalysis> findSimilar(AnalysisResult current, int topN) {
        // 1. 生成当前分析的嵌入向量
        float[] currentEmbedding = embeddingService.generateAnalysisEmbedding(current);
        
        // 2. SQL向量检索
        String sql = """
            SELECT 
                task_id,
                stock_code,
                analysis_date,
                recommendation,
                confidence,
                1 - (embedding <=> ?) AS similarity
            FROM analysis_history
            WHERE stock_code != ?
            ORDER BY embedding <=> ?
            LIMIT ?
            """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> SimilarAnalysis.builder()
                .taskId(rs.getString("task_id"))
                .stockCode(rs.getString("stock_code"))
                .analysisDate(rs.getDate("analysis_date").toLocalDate())
                .recommendation(rs.getString("recommendation"))
                .confidence(rs.getDouble("confidence"))
                .similarity(rs.getDouble("similarity"))
                .build(),
            currentEmbedding,
            current.getStockCode(),
            currentEmbedding,
            topN
        );
    }
    
    @Override
    public List<AnalysisHistory> findHistory(String stockCode, int limit) {
        String sql = """
            SELECT * FROM analysis_history
            WHERE stock_code = ?
            ORDER BY analysis_date DESC
            LIMIT ?
            """;
        
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> mapRow(rs),
            stockCode,
            limit
        );
    }
}
```

#### 6.3.4 分析历史服务
```java
// AnalysisHistoryService.java
@Service
public class AnalysisHistoryService {
    
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    private final SimilaritySearchService searchService;
    
    public void saveAnalysis(AnalysisResult result) {
        // 1. 生成唯一任务ID
        String taskId = UUID.randomUUID().toString();
        
        // 2. 生成嵌入向量
        float[] embedding = embeddingService.generateAnalysisEmbedding(result);
        
        // 3. 构建摘要
        String summary = buildSummary(result);
        
        // 4. SQL插入
        String sql = """
            INSERT INTO analysis_history (
                task_id, stock_code, stock_name, analysis_date,
                recommendation, risk_level, target_price, confidence,
                rsi, macd_signal, sentiment, sentiment_intensity, news_count,
                embedding, summary, key_factors, risk_factors
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            taskId,
            result.getStockCode(),
            result.getStockName(),
            LocalDate.now(),
            result.getInvestment().getRecommendation(),
            result.getInvestment().getRiskLevel(),
            result.getInvestment().getTargetPrice(),
            result.getInvestment().getConfidence(),
            result.getTechAnalyst().getRsi().getRsi(),
            result.getTechAnalyst().getRecommendation(),
            result.getSentiment().getOverallSentiment(),
            result.getSentiment().getSentimentIntensity(),
            result.getSentiment().getNews().size(),
            embedding,
            summary,
            String.join(";", result.getInvestment().getKeyFactors()),
            String.join(";", result.getInvestment().getRiskFactors())
        );
    }
    
    public SimilarAnalysisResponse findSimilarAnalyses(AnalysisResult current) {
        List<SimilarAnalysis> similar = searchService.findSimilar(current, 5);
        
        // 计算胜率统计
        long winCount = similar.stream()
            .filter(s -> isWin(s.getRecommendation(), s.getConfidence()))
            .count();
        
        double winRate = similar.isEmpty() ? 0 : (double) winCount / similar.size();
        
        return SimilarAnalysisResponse.builder()
            .similarAnalyses(similar)
            .winRate(winRate)
            .sampleSize(similar.size())
            .recommendation(winRate > 0.6 ? "买入" : winRate > 0.4 ? "持有" : "观望")
            .build();
    }
    
    private boolean isWin(String recommendation, double confidence) {
        return "买入".equals(recommendation) && confidence > 70;
    }
}
```

### 6.4 投资经理集成
```java
@Service
public class InvestmentManagerAgent {
    
    private final DebateController debateController;
    private final AnalysisHistoryService historyService;
    
    public InvestmentResult decide(AnalysisContext context) {
        // 1. 辩论结果
        DebateResult debate = debateController.runDebate(context);
        
        // 2. 查找相似历史
        SimilarAnalysisResponse similar = historyService.findSimilarAnalyses(context.toResult());
        
        // 3. 综合判断
        String finalRecommendation = blendWithHistory(debate, similar);
        double finalConfidence = adjustConfidence(debate.getConfidence(), similar);
        
        // 4. 保存分析历史
        historyService.saveAnalysis(buildResult(debate, similar));
        
        return InvestmentResult.builder()
            .recommendation(finalRecommendation)
            .confidence(finalConfidence)
            .similarAnalyses(similar.getSimilarAnalyses())
            .historicalWinRate(similar.getWinRate())
            .build();
    }
    
    private String blendWithHistory(DebateResult debate, SimilarAnalysisResponse similar) {
        double debateScore = "买入".equals(debate.getConsensus()) ? 0.8 : 
                            "持有".equals(debate.getConsensus()) ? 0.5 : 0.3;
        
        double historyScore = similar.getWinRate();
        
        double finalScore = debateScore * 0.7 + historyScore * 0.3;
        
        if (finalScore > 0.65) return "买入";
        if (finalScore > 0.45) return "持有";
        return "观望";
    }
}
```

### 6.5 前端历史展示
```vue
<template>
  <el-card>
    <template #header>
      <span>📊 历史相似分析</span>
    </template>
    
    <div class="historical-stats">
      <el-statistic title="历史胜率" :value="winRate * 100" suffix="%">
        <template #suffix>
          <el-progress
            :percentage="winRate * 100"
            :color="winRateColor"
            style="width: 100px; display: inline-block; margin-left: 10px;"
          />
        </template>
      </el-statistic>
    </div>
    
    <el-divider />
    
    <div class="similar-list">
      <el-timeline>
        <el-timeline-item
          v-for="item in similarAnalyses"
          :key="item.taskId"
          :type="item.type"
          :timestamp="item.date"
          placement="top"
        >
          <el-card>
            <h4>{{ item.stockCode }}</h4>
            <p>建议：{{ item.recommendation }}</p>
            <p>置信度：{{ item.confidence }}%</p>
            <p>相似度：{{ (item.similarity * 100).toFixed(1) }}%</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
    
    <div v-if="similarAnalyses.length === 0" class="empty-state">
      <el-empty description="暂无历史相似分析" />
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  similarData: Object
})

const similarAnalyses = computed(() => props.similarData?.similarAnalyses || [])
const winRate = computed(() => props.similarData?.winRate || 0)

const winRateColor = computed(() => {
  if (winRate.value > 0.6) return '#67C23A'
  if (winRate.value > 0.4) return '#E6A23C'
  return '#F56C6C'
})
</script>
```

### 6.6 新增API
```yaml
GET /api/history/similar/{stockCode}      # 查找相似分析
GET /api/history/stock/{stockCode}       # 股票分析历史
GET /api/history/recent                  # 最近分析
POST /api/history/save                    # 手动保存分析
DELETE /api/history/{taskId}             # 删除分析记录
```

### 6.7 验证标准
- [ ] PostgreSQL成功安装并启动
- [ ] pgvector扩展启用
- [ ] 分析结果成功存入数据库
- [ ] 向量检索返回相似结果
- [ ] 相似度计算合理（相似的分析应该相似度高）

### 6.8 风险与应对
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| pgvector性能问题 | 低 | 中 | 优化索引参数 + 分区表 |
| 嵌入服务不稳定 | 中 | 中 | 本地缓存 + 重试机制 |
| 数据量增长过快 | 中 | 中 | 定期清理 + 归档策略 |
| 向量维度不匹配 | 低 | 高 | 严格校验 + 异常日志 |

### 6.9 预估工时
- PostgreSQL部署：0.5天
- pgvector集成：0.5天
- 嵌入服务：1天
- 检索服务：1天
- Agent集成：0.5天
- 前端展示：0.5天
- 测试与优化：1天
- **总计：5天**

---

## 🐳 迭代7：生产就绪版

### 7.1 迭代目标
将系统优化为生产环境可用，包含容器化、监控、高可用等特性。

### 7.2 新增功能
- 🐳 **Docker容器化**：前后端Docker镜像
- 🔄 **负载均衡**：Nginx + 多实例部署
- 📊 **监控告警**：Prometheus + Grafana
- 🔒 **安全加固**：JWT认证 + 限流熔断
- 📝 **日志管理**：结构化日志 + ELK
- 🚀 **性能优化**：缓存优化 + 异步处理

### 7.3 技术架构

#### 7.3.1 Docker Compose配置
```yaml
version: '3.8'

services:
  # Nginx反向代理
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - backend
      - frontend
    networks:
      - stock-network

  # 后端服务
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/stock_analysis
      - ZHIPU_API_KEY=${ZHIPU_API_KEY}
    depends_on:
      - redis
      - postgres
    networks:
      - stock-network
    deploy:
      replicas: 2
      restart_policy:
        condition: on-failure

  # 前端服务
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    networks:
      - stock-network

  # Redis缓存
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - stock-network

  # PostgreSQL + pgvector
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      - POSTGRES_USER=stock_user
      - POSTGRES_PASSWORD=stock_pass
      - POSTGRES_DB=stock_analysis
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - stock-network

  # Prometheus监控
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
    networks:
      - stock-network

  # Grafana可视化
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    volumes:
      - grafana-data:/var/lib/grafana
    networks:
      - stock-network

networks:
  stock-network:
    driver: bridge

volumes:
  redis-data:
  postgres-data:
  grafana-data:
```

#### 7.3.2 Nginx配置
```nginx
upstream backend {
    least_conn;
    server backend:8080 weight=5;
}

upstream frontend {
    server frontend:3000;
}

server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    location / {
        proxy_pass http://frontend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # API代理
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # SSE支持
        proxy_http_version 1.1;
        proxy_set_header Connection '';
        chunked_transfer_encoding on;
    }

    # WebSocket支持（可选）
    location /ws/ {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 限流
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req zone=api burst=20 nodelay;
}
```

#### 7.3.3 安全配置
```java
// SecurityConfig.java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

// RateLimitFilter.java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) 
                                    throws ServletException, IOException {
        String clientId = getClientId(request);
        String key = "rate_limit:" + clientId;
        
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count != null && count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        
        if (count != null && count > 100) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

#### 7.3.4 监控配置
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'stock-analysis-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']
```

```java
// Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 7.4 高可用设计

#### 7.4.1 熔断降级
```java
// CircuitBreakerConfig.java
@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreakerFactory circuitBreakerFactory() {
        return new CircuitBreakerFactory();
    }
}

// LLM调用熔断
@Service
public class CircuitBreakerLLMGateway implements LLMGateway {
    
    private final LLMGateway delegate;
    private final CircuitBreaker<Float> circuitBreaker;
    
    public CircuitBreakerLLMGateway(LLMGateway delegate) {
        this.delegate = delegate;
        this.circuitBreaker = CircuitBreaker.of("llm", 
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(80)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build()
        );
    }
    
    @Override
    public String query(String prompt) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                return delegate.query(prompt);
            } catch (Exception e) {
                log.warn("LLM调用失败，返回降级结果", e);
                return getFallbackResponse();
            }
        });
    }
}
```

#### 7.4.2 异步任务队列
```java
// AsyncAnalysisService.java
@Service
public class AsyncAnalysisService {
    
    private final ExecutorService analysisExecutor;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public AsyncAnalysisService() {
        this.analysisExecutor = Executors.newFixedThreadPool(10);
    }
    
    public String submitAnalysis(String stockCode) {
        String taskId = UUID.randomUUID().toString();
        
        // 异步执行
        CompletableFuture.runAsync(() -> {
            try {
                // 更新状态
                updateTaskStatus(taskId, "RUNNING");
                
                // 执行分析
                AnalysisResult result = orchestrator.execute(stockCode);
                
                // 保存结果
                redisTemplate.opsForValue().set(
                    "task:" + taskId + ":result", 
                    result, 
                    1, TimeUnit.DAYS
                );
                
                updateTaskStatus(taskId, "COMPLETED");
                
            } catch (Exception e) {
                log.error("分析任务失败", e);
                updateTaskStatus(taskId, "FAILED");
            }
        }, analysisExecutor);
        
        return taskId;
    }
    
    public AnalysisResult getTaskResult(String taskId) {
        return (AnalysisResult) redisTemplate.opsForValue().get(
            "task:" + taskId + ":result"
        );
    }
}
```

### 7.5 日志管理
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/stock-analysis.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/stock-analysis-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 7.6 CI/CD配置
```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v1
      
      - name: Login to Docker Hub
        uses: docker/login-action@v1
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Build and Push Backend
        uses: docker/build-push-action@v2
        with:
          context: ./backend
          push: true
          tags: stockanalysis/backend:latest
      
      - name: Build and Push Frontend
        uses: docker/build-push-action@v2
        with:
          context: ./frontend
          push: true
          tags: stockanalysis/frontend:latest
      
      - name: Deploy to Server
        run: |
          ssh user@server "cd /opt/stock-analysis && docker-compose pull && docker-compose up -d"
```

### 7.7 验证标准
- [ ] Docker Compose一键启动成功
- [ ] Nginx负载均衡正常
- [ ] Prometheus监控数据采集成功
- [ ] Grafana仪表盘可访问
- [ ] JWT认证正常工作
- [ ] 限流规则生效
- [ ] 日志正常输出
- [ ] 熔断降级测试通过

### 7.8 风险与应对
| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| Docker网络问题 | 低 | 中 | 网络诊断工具 + 文档 |
| 数据库连接池耗尽 | 中 | 高 | 连接池监控 + 自动扩容 |
| 内存泄漏 | 中 | 高 | JVM监控 + 定期重启 |
| 磁盘空间不足 | 中 | 高 | 监控告警 + 自动清理 |

### 7.9 预估工时
- Docker配置：0.5天
- Nginx负载均衡：0.5天
- 监控部署：1天
- 安全加固：1天
- 日志系统：0.5天
- CI/CD配置：1天
- 性能优化：1天
- **总计：5.5天**

---

## 📅 完整迭代计划

### 时间线总览

```
Week 1-2: 迭代1（MVP基础版）✅
          迭代2（数据增强版）🔄 进行中
          ↓
Week 3:   迭代2完成 + 迭代3启动
          ↓
Week 4:   迭代3（AI初探版）
          ↓
Week 5:   迭代4（舆情增强版）
          ↓
Week 6-7: 迭代5（多模型辩论版）
          ↓
Week 8:   迭代6（向量检索版）
          ↓
Week 9-10: 迭代7（生产就绪版）
          ↓
Week 10+: 持续优化 & 新功能开发
```

### 里程碑

| 里程碑 | 完成时间 | 验收标准 |
|--------|----------|----------|
| M1: MVP可用 | Week 2 | MVP基础版正常运行 |
| M2: 数据真实 | Week 3 | 真实行情数据展示 |
| M3: AI增强 | Week 5 | LLM生成分析建议 |
| M4: 舆情完善 | Week 6 | 真实舆情分析 |
| M5: 智能决策 | Week 8 | 多模型辩论决策 |
| M6: 历史复用 | Week 9 | 相似性检索 |
| M7: 生产就绪 | Week 11 | 可部署、可监控 |

### 每个迭代的交付物

| 迭代 | 代码 | 文档 | 测试 | 部署 |
|------|------|------|------|------|
| I1 | ✅ | ✅ README | ✅ 冒烟测试 | ✅ 本地运行 |
| I2 | ✅ | ✅ 更新API文档 | ✅ 单元测试 | ✅ 本地+Docker |
| I3 | ✅ | ✅ 提示词设计文档 | ✅ 集成测试 | ✅ 本地 |
| I4 | ✅ | ✅ 舆情分析文档 | ✅ 情感分析测试 | ✅ 本地 |
| I5 | ✅ | ✅ 辩论机制设计 | ✅ 辩论测试 | ✅ 本地 |
| I6 | ✅ | ✅ 向量检索文档 | ✅ 检索测试 | ✅ Docker |
| I7 | ✅ | ✅ 运维手册 | ✅ 压力测试 | ✅ 生产环境 |

---

## 🎯 核心功能验证清单

### 基础功能
- [ ] 4个Agent流水线正常工作
- [ ] MACD/RSI指标计算准确
- [ ] K线图正确渲染
- [ ] SSE进度推送正常
- [ ] 自选股增删改查正常
- [ ] PDF报告生成

### 数据功能
- [ ] 真实K线数据获取
- [ ] 实时行情展示
- [ ] Redis缓存工作
- [ ] MySQL持久化成功

### AI功能
- [ ] LLM API调用成功
- [ ] AI生成投资建议
- [ ] 多模型并行调用
- [ ] 辩论机制正常
- [ ] 仲裁裁决合理

### 舆情功能
- [ ] 财经新闻抓取
- [ ] 情感分析准确
- [ ] 热点事件识别

### 向量检索
- [ ] PostgreSQL启动
- [ ] pgvector启用
- [ ] 嵌入向量生成
- [ ] 相似性检索准确

### 生产环境
- [ ] Docker容器化
- [ ] 负载均衡
- [ ] 监控告警
- [ ] 安全认证
- [ ] 限流熔断

---

## 📚 参考资料

### 技术文档
- [Spring Boot 3.2 Documentation](https://spring.io/projects/spring-boot)
- [SpringAI Documentation](https://spring.io/projects/spring-ai)
- [AKShare Python文档](https://akshare.akfamily.xyz/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [Vue 3 Documentation](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)

### API文档
- [智谱AI开放平台](https://open.bigmodel.cn/)
- [通义千问API](https://qwenlm.github.io/)
- [东方财富API](https://www.eastmoney.com/)

---

*文档版本: v2.0*
*创建时间: 2024-01-16*
*最后更新: 2024-01-16*
*作者: AI Architecture Assistant*
