package com.stock.analysis.service.agent;

import com.stock.analysis.model.dto.SentimentResult;
import com.stock.analysis.service.mock.MockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 舆情分析师Agent
 */
@Service
@RequiredArgsConstructor
public class SentimentAgent {
    
    private final MockDataService mockDataService;
    
    public SentimentResult analyze(String stockCode) {
        return SentimentResult.builder()
            .news(mockDataService.getDemoNews())
            .overallSentiment("积极")
            .build();
    }
}
