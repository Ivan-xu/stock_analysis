package com.stock.analysis.service.agent;

import com.stock.analysis.model.dto.SentimentResult;
import com.stock.analysis.service.sentiment.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAgent {

    private final NewsService newsService;

    public SentimentResult analyze(String stockCode) {
        log.info("SentimentAgent: 开始分析股票 {} 的舆情", stockCode);

        Map<String, Object> sentimentData = newsService.analyzeSentiment(stockCode);

        List<Map<String, String>> newsList = new ArrayList<>();
        Object newsObj = sentimentData.get("news");
        if (newsObj instanceof List) {
            List<?> rawNews = (List<?>) newsObj;
            for (Object item : rawNews) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rawItem = (Map<String, Object>) item;
                    Map<String, String> newsItem = new HashMap<>();
                    newsItem.put("date", String.valueOf(rawItem.getOrDefault("date", "")));
                    newsItem.put("title", String.valueOf(rawItem.getOrDefault("title", "")));
                    newsItem.put("content", String.valueOf(rawItem.getOrDefault("content", "")));
                    newsItem.put("sentiment", String.valueOf(rawItem.getOrDefault("sentiment", "neutral")));
                    newsItem.put("source", String.valueOf(rawItem.getOrDefault("source", "未知")));
                    newsList.add(newsItem);
                }
            }
        }

        String overallSentiment = String.valueOf(sentimentData.getOrDefault("overallSentiment", "中性"));

        int positiveCount = 0;
        int negativeCount = 0;
        for (Map<String, String> news : newsList) {
            String sentiment = news.getOrDefault("sentiment", "neutral");
            if ("positive".equals(sentiment)) {
                positiveCount++;
            } else if ("negative".equals(sentiment)) {
                negativeCount++;
            }
        }

        if (positiveCount > negativeCount * 1.5) {
            overallSentiment = "积极";
        } else if (negativeCount > positiveCount * 1.5) {
            overallSentiment = "消极";
        } else {
            overallSentiment = "中性";
        }

        log.info("SentimentAgent: 获取到 {} 条新闻，整体舆情: {}", newsList.size(), overallSentiment);

        return SentimentResult.builder()
            .news(newsList)
            .overallSentiment(overallSentiment)
            .positiveCount(positiveCount)
            .negativeCount(negativeCount)
            .neutralCount(newsList.size() - positiveCount - negativeCount)
            .build();
    }
}
