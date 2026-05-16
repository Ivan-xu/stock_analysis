package com.stock.analysis.service.market;

import com.stock.analysis.model.dto.KLineDTO;
import com.stock.analysis.model.dto.RealTimeQuoteDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketDataService {
    List<KLineDTO> getHistoricalKLine(String stockCode, LocalDate startDate, LocalDate endDate);
    Optional<RealTimeQuoteDTO> getRealTimeQuote(String stockCode);
    List<RealTimeQuoteDTO> batchGetQuotes(List<String> stockCodes);
    Optional<RealTimeQuoteDTO> getCachedQuote(String stockCode);
    void refreshQuote(String stockCode);
}
