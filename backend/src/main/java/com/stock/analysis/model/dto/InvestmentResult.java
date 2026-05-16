package com.stock.analysis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentResult {
    private String recommendation;
    private String riskLevel;
    private String targetPrice;
    private String reason;
    private Boolean aiGenerated;
    private Double confidence;
}
