package com.stock.analysis.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.analysis.model.dto.*;
import com.stock.analysis.service.llm.LLMGateway;
import com.stock.analysis.service.llm.Prompts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

/**
 * 投资经理Agent - AI增强版
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentManagerAgent {
    
    private final LLMGateway llmGateway;
    private final ObjectMapper objectMapper;
    private static final Random RANDOM = new Random();
    
    public InvestmentResult decide(TechResult tech, SentimentResult sentiment, ResearcherResult researcher) {
        log.info("InvestmentManagerAgent: 开始生成投资建议");
        
        // 生成提示词
        String prompt = Prompts.investmentAdvicePrompt(tech, sentiment, researcher);
        
        try {
            // 调用AI生成投资建议
            if (llmGateway.isAvailable()) {
                log.info("调用AI生成投资建议...");
                String aiResponse = llmGateway.query(prompt);
                
                if (aiResponse != null && !aiResponse.contains("AI服务暂时不可用")) {
                    return parseAIResponse(aiResponse, tech, sentiment);
                }
            } else {
                log.warn("AI服务不可用，使用规则引擎生成投资建议");
            }
        } catch (Exception e) {
            log.error("AI调用失败，使用规则引擎降级：{}", e.getMessage());
        }
        
        // 降级到规则引擎
        return generateRuleBasedResult(tech, sentiment);
    }
    
    /**
     * 解析AI响应
     */
    private InvestmentResult parseAIResponse(String aiResponse, TechResult tech, SentimentResult sentiment) {
        try {
            log.info("AI响应内容：{}", aiResponse);
            
            // 尝试解析JSON响应
            if (aiResponse.contains("{")) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}");
                String jsonStr = aiResponse.substring(start, end + 1);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);
                
                String recommendation = String.valueOf(result.getOrDefault("recommendation", "观望"));
                String riskLevel = String.valueOf(result.getOrDefault("riskLevel", "中风险"));
                String targetPrice = String.valueOf(result.getOrDefault("targetPrice", ""));
                String reasoning = String.valueOf(result.getOrDefault("reasoning", ""));
                
                log.info("AI建议：{}，风险：{}", recommendation, riskLevel);
                
                return InvestmentResult.builder()
                    .recommendation(recommendation)
                    .riskLevel(riskLevel)
                    .targetPrice(targetPrice)
                    .reason(ArrayToString(reasoning))
                    .aiGenerated(true)
                    .confidence(85.0)
                    .build();
            }
        } catch (Exception e) {
            log.error("解析AI响应失败：{}", e.getMessage());
        }
        
        // 解析失败，降级到规则引擎
        return generateRuleBasedResult(tech, sentiment);
    }
    
    /**
     * 将数组转换为字符串
     */
    private String ArrayToString(Object obj) {
        if (obj instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<String> list = (java.util.List<String>) obj;
            return String.join("；", list);
        }
        return String.valueOf(obj);
    }
    
    /**
     * 规则引擎生成投资建议（降级方案）
     */
    private InvestmentResult generateRuleBasedResult(TechResult tech, SentimentResult sentiment) {
        log.info("使用规则引擎生成投资建议...");
        
        double techScore = analyzeTech(tech);
        double sentimentScore = analyzeSentiment(sentiment);
        double totalScore = (techScore + sentimentScore) / 2;
        
        String recommendation;
        String riskLevel;
        if (totalScore > 0.6) {
            recommendation = "买入";
            riskLevel = "低风险";
        } else if (totalScore > 0.4) {
            recommendation = "持有";
            riskLevel = "中等风险";
        } else {
            recommendation = "观望";
            riskLevel = "高风险";
        }
        
        double targetBase = techScore * 2000;
        double targetPrice = targetBase + (RANDOM.nextDouble() - 0.5) * 200;
        
        return InvestmentResult.builder()
            .recommendation(recommendation)
            .riskLevel(riskLevel)
            .targetPrice(String.format("%.2f", targetPrice))
            .reason(String.format("综合技术面（%.0f分）和舆情（%.0f分），给出投资建议（规则引擎生成）", techScore * 100, sentimentScore * 100))
            .aiGenerated(false)
            .confidence(totalScore * 100)
            .build();
    }
    
    private double analyzeTech(TechResult tech) {
        double rsi = tech.getRsi().getRsi();
        return (100 - rsi) / 100;
    }
    
    private double analyzeSentiment(SentimentResult sentiment) {
        int total = sentiment.getPositiveCount() + sentiment.getNegativeCount() + sentiment.getNeutralCount();
        if (total == 0) return 0.5;
        
        return (double) sentiment.getPositiveCount() / total;
    }
}
