package com.stock.analysis.service.agent;

import com.stock.analysis.model.dto.ResearcherResult;
import com.stock.analysis.model.dto.RealTimeQuoteDTO;
import com.stock.analysis.service.market.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResearcherAgent {

    private final MarketDataService marketDataService;

    private static final Map<String, String> STOCK_NAMES = Map.of(
        "600519", "贵州茅台",
        "000858", "五粮液",
        "601318", "中国平安",
        "000001", "平安银行",
        "600036", "招商银行",
        "000333", "美的集团",
        "002594", "比亚迪",
        "600276", "恒瑞医药",
        "300750", "宁德时代",
        "688981", "中芯国际"
    );

    public ResearcherResult analyze(String stockCode) {
        log.info("ResearcherAgent: 开始分析股票 {}", stockCode);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);

        var kLines = marketDataService.getHistoricalKLine(stockCode, startDate, endDate);

        String stockName = STOCK_NAMES.getOrDefault(stockCode, "未知股票");
        String industry = "未知行业";

        RealTimeQuoteDTO quote = marketDataService.getRealTimeQuote(stockCode).orElse(null);

        if (quote != null && quote.getStockName() != null) {
            stockName = quote.getStockName();
        }

        log.info("ResearcherAgent: 获取到 {} 条K线数据", kLines.size());

        return ResearcherResult.builder()
            .stockCode(stockCode)
            .stockName(stockName)
            .industry(industry)
            .kLines(kLines)
            .realTimeQuote(quote)
            .build();
    }

    public ResearcherResult analyzeWithCache(String stockCode) {
        log.info("ResearcherAgent: 使用缓存分析股票 {}", stockCode);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);

        var kLines = marketDataService.getHistoricalKLine(stockCode, startDate, endDate);

        String stockName = STOCK_NAMES.getOrDefault(stockCode, "未知股票");
        RealTimeQuoteDTO quote = marketDataService.getCachedQuote(stockCode).orElse(null);

        if (quote != null && quote.getStockName() != null) {
            stockName = quote.getStockName();
        }

        return ResearcherResult.builder()
            .stockCode(stockCode)
            .stockName(stockName)
            .industry("未知行业")
            .kLines(kLines)
            .realTimeQuote(quote)
            .build();
    }
}
