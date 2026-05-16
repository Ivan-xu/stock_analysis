package com.stock.analysis.service.sentiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class NewsService {

    @Value("${akshare.base-url}")
    private String akshareBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NewsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Map<String, Object>> fetchNews(String stockCode) {
        try {
            String url = akshareBaseUrl + "/api/news/" + stockCode + "?count=10";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                Object dataObj = response.get("data");
                if (dataObj instanceof List) {
                    List<?> rawList = (List<?>) dataObj;
                    List<Map<String, Object>> newsList = new ArrayList<>();
                    
                    for (Object item : rawList) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> newsItem = (Map<String, Object>) item;
                            newsList.add(newsItem);
                        }
                    }
                    
                    log.info("Fetched {} news items for {} from {}",
                        newsList.size(), stockCode, response.get("source"));
                    return newsList;
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch news for {}: {}", stockCode, e.getMessage());
        }

        return Collections.emptyList();
    }

    public Map<String, Object> analyzeSentiment(String stockCode) {
        try {
            String url = akshareBaseUrl + "/api/sentiment/" + stockCode;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                Object dataObj = response.get("data");
                if (dataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sentimentData = (Map<String, Object>) dataObj;
                    log.info("Sentiment analysis for {}: {} (score: {})",
                        stockCode,
                        sentimentData.get("overallSentiment"),
                        sentimentData.get("sentimentScore"));
                    return sentimentData;
                }
            }
        } catch (Exception e) {
            log.error("Failed to analyze sentiment for {}: {}", stockCode, e.getMessage());
        }

        return createDefaultSentiment(stockCode);
    }

    private Map<String, Object> createDefaultSentiment(String stockCode) {
        Map<String, Object> defaultSentiment = new HashMap<>();
        defaultSentiment.put("stockCode", stockCode);
        defaultSentiment.put("overallSentiment", "中性");
        defaultSentiment.put("sentimentScore", 0.5);
        defaultSentiment.put("positiveCount", 0);
        defaultSentiment.put("negativeCount", 0);
        defaultSentiment.put("neutralCount", 0);
        defaultSentiment.put("totalNews", 0);
        defaultSentiment.put("news", Collections.emptyList());
        return defaultSentiment;
    }
}
