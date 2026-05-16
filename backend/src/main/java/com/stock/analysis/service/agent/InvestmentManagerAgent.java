package com.stock.analysis.service.agent;

import com.stock.analysis.model.dto.InvestmentResult;
import com.stock.analysis.model.dto.SentimentResult;
import com.stock.analysis.model.dto.TechResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 投资经理Agent
 */
@Service
@RequiredArgsConstructor
public class InvestmentManagerAgent {
    
    private static final Random RANDOM = new Random();
    
    public InvestmentResult decide(TechResult tech, SentimentResult sentiment) {
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
        
        double targetBase = 1850 + (RANDOM.nextDouble() - 0.5) * 200;
        
        return InvestmentResult.builder()
            .recommendation(recommendation)
            .riskLevel(riskLevel)
            .targetPrice(String.format("%.2f", targetBase))
            .reason(String.format("综合技术面（%.0f分）和舆情（%.0f分），给出投资建议", techScore * 100, sentimentScore * 100))
            .build();
    }
    
    private double analyzeTech(TechResult tech) {
        double rsi = tech.getRsi().getRsi();
        return (100 - rsi) / 100;
    }
    
    private double analyzeSentiment(SentimentResult sentiment) {
        return "积极".equals(sentiment.getOverallSentiment()) ? 0.8 : 0.5;
    }
}
