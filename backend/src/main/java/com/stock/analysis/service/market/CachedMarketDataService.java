package com.stock.analysis.service.market;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.analysis.model.dto.KLineDTO;
import com.stock.analysis.model.dto.RealTimeQuoteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CachedMarketDataService implements MarketDataService {

    private static final String QUOTE_KEY_PREFIX = "quote:";
    private static final String KLINES_KEY_PREFIX = "klines:";
    private static final long QUOTE_CACHE_TTL = 60;
    private static final long KLINES_CACHE_TTL = 3600;

    @Autowired
    private AKShareMarketDataService akshareService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<KLineDTO> getHistoricalKLine(String stockCode, LocalDate startDate, LocalDate endDate) {
        String cacheKey = KLINES_KEY_PREFIX + stockCode + ":" + startDate + ":" + endDate;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("Cache hit for K-lines: {}", stockCode);
                return convertToKLines(cached);
            }
        } catch (Exception e) {
            log.warn("Failed to read K-lines from cache: {}", e.getMessage());
        }

        log.info("Fetching K-lines from AKShare for {}", stockCode);
        List<KLineDTO> kLines = akshareService.getHistoricalKLine(stockCode, startDate, endDate);

        if (!kLines.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, kLines, KLINES_CACHE_TTL, TimeUnit.SECONDS);
                log.info("Cached K-lines for {}: {} records", stockCode, kLines.size());
            } catch (Exception e) {
                log.warn("Failed to cache K-lines: {}", e.getMessage());
            }
        }

        return kLines;
    }

    @Override
    public Optional<RealTimeQuoteDTO> getRealTimeQuote(String stockCode) {
        String cacheKey = QUOTE_KEY_PREFIX + stockCode;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for quote: {}", stockCode);
                return Optional.of(convertToQuote(cached));
            }
        } catch (Exception e) {
            log.warn("Failed to read quote from cache: {}", e.getMessage());
        }

        log.info("Fetching quote from AKShare for {}", stockCode);
        Optional<RealTimeQuoteDTO> quote = akshareService.getRealTimeQuote(stockCode);

        quote.ifPresent(q -> {
            try {
                redisTemplate.opsForValue().set(cacheKey, q, QUOTE_CACHE_TTL, TimeUnit.SECONDS);
                log.debug("Cached quote for {}", stockCode);
            } catch (Exception e) {
                log.warn("Failed to cache quote: {}", e.getMessage());
            }
        });

        return quote;
    }

    @Override
    public List<RealTimeQuoteDTO> batchGetQuotes(List<String> stockCodes) {
        return akshareService.batchGetQuotes(stockCodes);
    }

    @Override
    public Optional<RealTimeQuoteDTO> getCachedQuote(String stockCode) {
        String cacheKey = QUOTE_KEY_PREFIX + stockCode;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Optional.of(convertToQuote(cached));
            }
        } catch (Exception e) {
            log.warn("Failed to read cached quote: {}", e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public void refreshQuote(String stockCode) {
        String cacheKey = QUOTE_KEY_PREFIX + stockCode;

        try {
            redisTemplate.delete(cacheKey);
            log.info("Cache cleared for {}", stockCode);

            Optional<RealTimeQuoteDTO> quote = akshareService.getRealTimeQuote(stockCode);
            quote.ifPresent(q -> {
                try {
                    redisTemplate.opsForValue().set(cacheKey, q, QUOTE_CACHE_TTL, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("Failed to refresh quote cache: {}", e.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Failed to refresh quote for {}: {}", stockCode, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<KLineDTO> convertToKLines(Object cached) {
        if (cached instanceof List) {
            try {
                String json = objectMapper.writeValueAsString(cached);
                return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, KLineDTO.class));
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize K-lines: {}", e.getMessage());
                return List.of();
            }
        }

        try {
            String json = objectMapper.writeValueAsString(cached);
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, KLineDTO.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize K-lines: {}", e.getMessage());
            return List.of();
        }
    }

    private RealTimeQuoteDTO convertToQuote(Object cached) {
        if (cached instanceof RealTimeQuoteDTO) {
            return (RealTimeQuoteDTO) cached;
        }

        try {
            String json = objectMapper.writeValueAsString(cached);
            return objectMapper.readValue(json, RealTimeQuoteDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize quote: {}", e.getMessage());
            return null;
        }
    }
}
