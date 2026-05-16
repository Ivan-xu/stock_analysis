package com.stock.analysis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResult {
    private List<Map<String, String>> news;
    private String overallSentiment;
    private Integer positiveCount;
    private Integer negativeCount;
    private Integer neutralCount;
}
