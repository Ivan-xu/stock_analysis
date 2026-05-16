package com.stock.analysis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearcherResult {
    private String stockCode;
    private String stockName;
    private String industry;
    private List<KLine> kLines;
    private RealTimeQuoteDTO realTimeQuote;
}
