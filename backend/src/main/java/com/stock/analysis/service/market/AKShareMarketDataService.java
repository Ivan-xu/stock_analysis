package com.stock.analysis.service.market;

import com.stock.analysis.model.dto.KLineDTO;
import com.stock.analysis.model.dto.RealTimeQuoteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class AKShareMarketDataService {

    private final RestTemplate restTemplate;
    private final String akshareBaseUrl;

    @Autowired
    public AKShareMarketDataService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(java.time.Duration.ofSeconds(10))
            .setReadTimeout(java.time.Duration.ofSeconds(30))
            .build();
        this.akshareBaseUrl = "http://localhost:5001";
    }

    public List<KLineDTO> getHistoricalKLine(String stockCode, LocalDate startDate, LocalDate endDate) {
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl(akshareBaseUrl + "/api/kline/" + stockCode)
                .queryParam("start", startDate.toString().replace("-", ""))
                .queryParam("end", endDate.toString().replace("-", ""))
                .build()
                .toUriString();

            log.info("Fetching K-line data for {} from {} to {}", stockCode, startDate, endDate);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    return parseKLines((List<Map<String, Object>>) body.get("data"));
                } else {
                    log.error("Failed to fetch K-line data: {}", body.get("error"));
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error fetching K-line data for {}: {}", stockCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<RealTimeQuoteDTO> getRealTimeQuote(String stockCode) {
        try {
            String url = akshareBaseUrl + "/api/quote/" + stockCode;

            log.info("Fetching real-time quote for {}", stockCode);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    return Optional.of(parseQuote((Map<String, Object>) body.get("data")));
                } else {
                    log.error("Failed to fetch quote: {}", body.get("error"));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching real-time quote for {}: {}", stockCode, e.getMessage());
            return Optional.empty();
        }
    }

    public List<RealTimeQuoteDTO> batchGetQuotes(List<String> stockCodes) {
        try {
            String url = akshareBaseUrl + "/api/batch-quote";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("codes", stockCodes);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) body.get("data");
                    return parseQuotes(dataList);
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error batch fetching quotes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<Map<String, Object>> getStockInfo(String stockCode) {
        try {
            String url = akshareBaseUrl + "/api/stock-info/" + stockCode;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    return Optional.of((Map<String, Object>) body.get("data"));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching stock info for {}: {}", stockCode, e.getMessage());
            return Optional.empty();
        }
    }

    private List<KLineDTO> parseKLines(List<Map<String, Object>> dataList) {
        List<KLineDTO> kLines = new ArrayList<>();

        for (Map<String, Object> data : dataList) {
            KLineDTO kLine = KLineDTO.builder()
                .date(data.get("date").toString())
                .open(parseDouble(data.get("open")))
                .high(parseDouble(data.get("high")))
                .low(parseDouble(data.get("low")))
                .close(parseDouble(data.get("close")))
                .volume(parseLong(data.get("volume")))
                .turnover(parseDouble(data.get("turnover")))
                .build();

            kLines.add(kLine);
        }

        return kLines;
    }

    private RealTimeQuoteDTO parseQuote(Map<String, Object> data) {
        return RealTimeQuoteDTO.builder()
            .stockCode(data.get("stockCode").toString())
            .stockName(data.get("stockName").toString())
            .currentPrice(parseDouble(data.get("currentPrice")))
            .changeAmount(parseDouble(data.get("changeAmount")))
            .changePercent(parseDouble(data.get("changePercent")))
            .openPrice(parseDouble(data.get("openPrice")))
            .highPrice(parseDouble(data.get("highPrice")))
            .lowPrice(parseDouble(data.get("lowPrice")))
            .volume(parseLong(data.get("volume")))
            .turnover(parseDouble(data.get("turnover")))
            .build();
    }

    private List<RealTimeQuoteDTO> parseQuotes(List<Map<String, Object>> dataList) {
        List<RealTimeQuoteDTO> quotes = new ArrayList<>();

        for (Map<String, Object> data : dataList) {
            quotes.add(parseQuote(data));
        }

        return quotes;
    }

    private Double parseDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
