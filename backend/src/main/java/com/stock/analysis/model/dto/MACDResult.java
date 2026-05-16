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
public class MACDResult {
    private List<Double> ema12;
    private List<Double> ema26;
    private List<Double> dif;
    private List<Double> dea;
    private List<Double> macdBar;
}
