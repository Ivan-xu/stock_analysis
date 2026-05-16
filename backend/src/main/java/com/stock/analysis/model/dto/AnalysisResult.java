package com.stock.analysis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String stockCode;
    private String stockName;
    private ResearcherResult researcher;
    private TechResult techAnalyst;
    private SentimentResult sentiment;
    private InvestmentResult investment;
}
