package com.stock.analysis.service.agent;

import com.stock.analysis.model.dto.ResearcherResult;
import com.stock.analysis.service.mock.MockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 研究员Agent - 行情数据采集
 */
@Service
@RequiredArgsConstructor
public class ResearcherAgent {
    
    private final MockDataService mockDataService;
    
    private static final Map<String, String> STOCK_NAMES = Map.of(
        "600519", "贵州茅台",
        "000858", "五粮液",
        "601318", "中国平安",
        "000001", "平安银行",
        "600036", "招商银行"
    );
    
    public ResearcherResult analyze(String stockCode) {
        String stockName = STOCK_NAMES.getOrDefault(stockCode, "未知股票");
        
        return ResearcherResult.builder()
            .stockCode(stockCode)
            .stockName(stockName)
            .industry("食品饮料")
            .kLines(mockDataService.getDemoKLines())
            .build();
    }
}
