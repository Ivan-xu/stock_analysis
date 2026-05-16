package com.stock.analysis.service.indicator;

import com.stock.analysis.model.dto.KLineDTO;
import com.stock.analysis.model.dto.MACDResult;
import com.stock.analysis.model.dto.RSIResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 技术指标计算服务
 */
@Service
public class TechnicalIndicatorService {
    
    /**
     * 计算MACD
     */
    public MACDResult calculateMACD(List<KLineDTO> kLines) {
        List<Double> closePrices = kLines.stream()
            .map(KLineDTO::getClose)
            .toList();
        
        List<Double> ema12 = calculateEMA(closePrices, 12);
        List<Double> ema26 = calculateEMA(closePrices, 26);
        List<Double> dif = calculateDIF(ema12, ema26);
        List<Double> dea = calculateEMA(dif, 9);
        List<Double> macdBar = calculateMACDBar(dif, dea);
        
        return MACDResult.builder()
            .ema12(ema12)
            .ema26(ema26)
            .dif(dif)
            .dea(dea)
            .macdBar(macdBar)
            .build();
    }
    
    /**
     * 计算RSI
     */
    public RSIResult calculateRSI(List<KLineDTO> kLines, int period) {
        List<Double> closePrices = kLines.stream()
            .map(KLineDTO::getClose)
            .toList();
        
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        
        for (int i = 1; i < closePrices.size(); i++) {
            double diff = closePrices.get(i) - closePrices.get(i - 1);
            gains.add(diff > 0 ? diff : 0);
            losses.add(diff < 0 ? -diff : 0);
        }
        
        double avgGain = average(gains, period);
        double avgLoss = average(losses, period);
        double rs = avgGain / (avgLoss == 0 ? 0.001 : avgLoss);
        double rsi = 100 - (100 / (1 + rs));
        
        String suggestion;
        if (rsi > 70) {
            suggestion = "超买区域，考虑卖出";
        } else if (rsi < 30) {
            suggestion = "超卖区域，考虑买入";
        } else {
            suggestion = "中性区域，持有";
        }
        
        return RSIResult.builder()
            .rsi(rsi)
            .suggestion(suggestion)
            .build();
    }
    
    private List<Double> calculateEMA(List<Double> data, int period) {
        List<Double> ema = new ArrayList<>();
        if (data.isEmpty()) return ema;
        
        double sma = data.stream().limit(period).mapToDouble(d -> d).average().orElse(0);
        ema.add(sma);
        
        double multiplier = 2.0 / (period + 1);
        
        for (int i = period; i < data.size(); i++) {
            double prevEMA = ema.get(ema.size() - 1);
            double current = data.get(i);
            ema.add((current - prevEMA) * multiplier + prevEMA);
        }
        
        while (ema.size() < data.size()) {
            ema.add(0, ema.get(0));
        }
        
        return ema;
    }
    
    private List<Double> calculateDIF(List<Double> ema12, List<Double> ema26) {
        List<Double> dif = new ArrayList<>();
        int size = Math.min(ema12.size(), ema26.size());
        for (int i = 0; i < size; i++) {
            dif.add(ema12.get(i) - ema26.get(i));
        }
        return dif;
    }
    
    private List<Double> calculateMACDBar(List<Double> dif, List<Double> dea) {
        List<Double> bars = new ArrayList<>();
        int size = Math.min(dif.size(), dea.size());
        for (int i = 0; i < size; i++) {
            bars.add((dif.get(i) - dea.get(i)) * 2);
        }
        return bars;
    }
    
    private double average(List<Double> data, int period) {
        int start = Math.max(0, data.size() - period);
        return data.subList(start, data.size()).stream()
            .mapToDouble(d -> d)
            .average()
            .orElse(0);
    }
}
