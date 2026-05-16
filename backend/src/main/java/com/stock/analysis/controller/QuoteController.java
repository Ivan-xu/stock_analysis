package com.stock.analysis.controller;

import com.stock.analysis.model.dto.RealTimeQuoteDTO;
import com.stock.analysis.service.market.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quote")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuoteController {

    private final MarketDataService marketDataService;

    @GetMapping("/{stockCode}")
    public ResponseEntity<Map<String, Object>> getQuote(@PathVariable String stockCode) {
        log.info("获取股票 {} 的实时行情", stockCode);

        try {
            return marketDataService.getRealTimeQuote(stockCode)
                .map(quote -> ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", quote
                )))
                .orElse(ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "无法获取行情数据"
                )));

        } catch (Exception e) {
            log.error("获取行情失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> getBatchQuotes(@RequestBody Map<String, List<String>> request) {
        List<String> codes = request.get("codes");

        if (codes == null || codes.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "股票代码列表不能为空"
            ));
        }

        log.info("批量获取 {} 只股票的行情", codes.size());

        try {
            List<RealTimeQuoteDTO> quotes = marketDataService.batchGetQuotes(codes);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", quotes,
                "count", quotes.size()
            ));

        } catch (Exception e) {
            log.error("批量获取行情失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/refresh/{stockCode}")
    public ResponseEntity<Map<String, Object>> refreshQuote(@PathVariable String stockCode) {
        log.info("刷新股票 {} 的行情缓存", stockCode);

        try {
            marketDataService.refreshQuote(stockCode);

            return marketDataService.getRealTimeQuote(stockCode)
                .map(quote -> ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "行情已刷新",
                    "data", quote
                )))
                .orElse(ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "刷新成功但获取失败"
                )));

        } catch (Exception e) {
            log.error("刷新行情失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
