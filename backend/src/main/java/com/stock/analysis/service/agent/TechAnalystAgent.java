package com.stock.analysis.service.agent;

import com.stock.analysis.model.dto.MACDResult;
import com.stock.analysis.model.dto.RSIResult;
import com.stock.analysis.model.dto.ResearcherResult;
import com.stock.analysis.model.dto.TechResult;
import com.stock.analysis.service.indicator.TechnicalIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 技术分析师Agent
 */
@Service
@RequiredArgsConstructor
public class TechAnalystAgent {
    
    private final TechnicalIndicatorService indicatorService;
    
    public TechResult analyze(ResearcherResult researcher) {
        MACDResult macd = indicatorService.calculateMACD(researcher.getKLines());
        RSIResult rsi = indicatorService.calculateRSI(researcher.getKLines(), 14);
        
        String recommendation;
        if (rsi.getRsi() < 40) {
            recommendation = "技术面偏多";
        } else if (rsi.getRsi() > 60) {
            recommendation = "技术面偏空";
        } else {
            recommendation = "技术面中性";
        }
        
        return TechResult.builder()
            .macd(macd)
            .rsi(rsi)
            .recommendation(recommendation)
            .build();
    }
}
