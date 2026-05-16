package com.stock.analysis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeQuoteDTO {
    private String stockCode;
    private String stockName;
    private Double currentPrice;
    private Double changeAmount;
    private Double changePercent;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Long volume;
    private Double turnover;
    private Double marketCap;
    private Double pe;
    private Long timestamp;
}
