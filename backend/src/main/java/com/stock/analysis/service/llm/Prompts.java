package com.stock.analysis.service.llm;

import com.stock.analysis.model.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板
 */
@Slf4j
public class Prompts {
    
    /**
     * 生成投资建议提示词
     */
    public static String investmentAdvicePrompt(
        TechResult tech, 
        SentimentResult sentiment, 
        ResearcherResult researcher
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为一位专业投资顾问，请结合以下分析结果给出投资建议：\n\n");
        
        // 技术面分析
        prompt.append("【技术面分析】\n");
        prompt.append(String.format("- RSI指标：%.2f\n", tech.getRsi().getRsi()));
        prompt.append(String.format("- RSI建议：%s\n", tech.getRsi().getSuggestion()));
        prompt.append(String.format("- MACD DIF最新值：%.2f\n", getLatestValue(tech.getMacd().getDif())));
        prompt.append(String.format("- 技术综合评分：%s\n", tech.getRecommendation()));
        prompt.append("\n");
        
        // 舆情分析
        prompt.append("【舆情分析】\n");
        prompt.append(String.format("- 整体舆情：%s\n", sentiment.getOverallSentiment()));
        prompt.append(String.format("- 新闻数量：%d条\n", sentiment.getNews().size()));
        prompt.append(String.format("- 正面新闻：%d条\n", sentiment.getPositiveCount()));
        prompt.append(String.format("- 负面新闻：%d条\n", sentiment.getNegativeCount()));
        prompt.append(String.format("- 中性新闻：%d条\n", sentiment.getNeutralCount()));
        prompt.append("\n");
        
        // 基本面数据
        if (researcher.getRealTimeQuote() != null) {
            prompt.append("【行情数据】\n");
            RealTimeQuoteDTO quote = researcher.getRealTimeQuote();
            prompt.append(String.format("- 当前价格：%.2f元\n", quote.getCurrentPrice()));
            prompt.append(String.format("- 涨跌额：%.2f元\n", quote.getChangeAmount()));
            prompt.append(String.format("- 涨跌幅：%.2f%%\n", quote.getChangePercent()));
            prompt.append("\n");
        }
        
        prompt.append("请给出：\n");
        prompt.append("1. 投资建议（买入/持有/观望/卖出）\n");
        prompt.append("2. 目标价格区间\n");
        prompt.append("3. 止损价格\n");
        prompt.append("4. 风险等级（低/中/高）\n");
        prompt.append("5. 核心投资逻辑（2-3句话）\n");
        prompt.append("\n");
        
        prompt.append("请用JSON格式返回，包含字段：\n");
        prompt.append("{\n");
        prompt.append('"').append("recommendation").append('"').append(": \"买入/持有/观望/卖出\",\n");
        prompt.append('"').append("targetPrice").append('"').append(": \"价格区间\",\n");
        prompt.append('"').append("stopLoss").append('"').append(": \"止损价格\",\n");
        prompt.append('"').append("riskLevel").append('"').append(": \"低/中/高\",\n");
        prompt.append('"').append("reasoning").append('"').append(": [\"逻辑1\", \"逻辑2\"]\n");
        prompt.append("}\n");
        
        log.info("生成投资建议提示词，长度：{}", prompt.length());
        return prompt.toString();
    }
    
    /**
     * 获取列表最新值
     */
    private static double getLatestValue(List<Double> list) {
        if (list == null || list.isEmpty()) {
            return 0.0;
        }
        return list.get(list.size() - 1);
    }
    
    /**
     * 生成基本面分析提示词
     */
    public static String fundamentalAnalysisPrompt(ResearcherResult researcher) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为一位资深金融分析师，请分析以下股票的基本面：\n\n");
        
        if (researcher.getRealTimeQuote() != null) {
            RealTimeQuoteDTO quote = researcher.getRealTimeQuote();
            prompt.append("【股票信息】\n");
            prompt.append(String.format("- 股票代码：%s\n", researcher.getStockCode()));
            prompt.append(String.format("- 股票名称：%s\n", researcher.getStockName()));
            prompt.append(String.format("- 当前价格：%.2f元\n", quote.getCurrentPrice()));
            prompt.append(String.format("- 涨跌幅：%.2f%%\n", quote.getChangePercent()));
            prompt.append("\n");
        }
        
        if (researcher.getKLines() != null && !researcher.getKLines().isEmpty()) {
            prompt.append("【K线数据统计】\n");
            List<KLineDTO> kLines = researcher.getKLines();
            
            double avgClose = kLines.stream()
                .mapToDouble(k -> k.getClose().doubleValue())
                .average()
                .orElse(0);
            
            double maxHigh = kLines.stream()
                .mapToDouble(k -> k.getHigh().doubleValue())
                .max()
                .orElse(0);
            
            double minLow = kLines.stream()
                .mapToDouble(k -> k.getLow().doubleValue())
                .min()
                .orElse(0);
            
            double avgVolume = kLines.stream()
                .mapToDouble(k -> k.getVolume().doubleValue())
                .average()
                .orElse(0);
            
            prompt.append(String.format("- 最近%d天数据\n", kLines.size()));
            prompt.append(String.format("- 平均收盘价：%.2f元\n", avgClose));
            prompt.append(String.format("- 最高价：%.2f元\n", maxHigh));
            prompt.append(String.format("- 最低价：%.2f元\n", minLow));
            prompt.append(String.format("- 平均成交量：%.2f\n", avgVolume));
            prompt.append("\n");
        }
        
        prompt.append("请从以下角度进行分析：\n");
        prompt.append("1. 股价走势分析\n");
        prompt.append("2. 成交量变化趋势\n");
        prompt.append("3. 主要风险点\n");
        prompt.append("4. 基本面评分（1-10分）\n");
        prompt.append("\n");
        
        prompt.append("请用JSON格式返回，包含字段：\n");
        prompt.append("{\n");
        prompt.append('"').append("analysis").append('"').append(": \"分析内容\",\n");
        prompt.append('"').append("riskPoints").append('"').append(": [\"风险1\", \"风险2\"],\n");
        prompt.append('"').append("score").append('"').append(": 8.5\n");
        prompt.append("}\n");
        
        log.info("生成基本面分析提示词，长度：{}", prompt.length());
        return prompt.toString();
    }
    
    /**
     * 生成新闻摘要提示词
     */
    public static String newsSummaryPrompt(SentimentResult sentiment) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下财经新闻的整体舆情：\n\n");
        
        int count = 0;
        for (Map<String, String> news : sentiment.getNews()) {
            count++;
            if (count > 10) break;  // 限制新闻数量
            
            prompt.append(String.format("新闻%d：%s\n", count, news.get("title")));
            prompt.append(String.format("情感：%s\n\n", news.get("sentiment")));
        }
        
        prompt.append("\n请给出：\n");
        prompt.append("1. 舆情总结（1-2句话）\n");
        prompt.append("2. 主要关注点\n");
        prompt.append("3. 投资建议（结合舆情）\n");
        
        return prompt.toString();
    }
}
