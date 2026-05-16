package com.stock.analysis.service.mock;

import com.stock.analysis.model.dto.KLine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mock数据服务
 */
@Service
public class MockDataService {
    
    public List<KLine> getDemoKLines() {
        List<KLine> kLines = new ArrayList<>();
        LocalDate date = LocalDate.now();
        BigDecimal basePrice = new BigDecimal("1800.00");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 0; i < 60; i++) {
            LocalDate tradeDate = date.minusDays(60 - i);
            
            double randomChange = (Math.random() - 0.5) * 100;
            BigDecimal open = basePrice.add(new BigDecimal(randomChange * 0.8));
            BigDecimal close = basePrice.add(new BigDecimal(randomChange));
            BigDecimal high = open.max(close).add(new BigDecimal(Math.random() * 20));
            BigDecimal low = open.min(close).subtract(new BigDecimal(Math.random() * 20));
            Long volume = (long) (1000000 + Math.random() * 500000);
            
            kLines.add(KLine.builder()
                .date(tradeDate.format(formatter))
                .open(open)
                .close(close)
                .high(high)
                .low(low)
                .volume(volume)
                .build());
            
            basePrice = close;
        }
        
        return kLines;
    }
    
    public List<Map<String, String>> getDemoNews() {
        return List.of(
            Map.of(
                "title", "公司发布2024年业绩预告",
                "content", "公司预计2024年净利润同比增长30%",
                "sentiment", "positive",
                "source", "财经网",
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ),
            Map.of(
                "title", "行业分析师上调评级",
                "content", "多家券商上调公司目标价至2000元",
                "sentiment", "positive",
                "source", "证券时报",
                "date", LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ),
            Map.of(
                "title", "新产品上市获得市场好评",
                "content", "公司新产品在市场上反响热烈",
                "sentiment", "positive",
                "source", "上海证券报",
                "date", LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
        );
    }
}
