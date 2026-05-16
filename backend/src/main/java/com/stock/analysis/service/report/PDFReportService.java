package com.stock.analysis.service.report;

import com.stock.analysis.model.dto.AnalysisResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class PDFReportService {
    
    private static final Map<String, String> CHINESE_TO_PINYIN = new HashMap<>();
    
    static {
        CHINESE_TO_PINYIN.put("贵州茅台", "Kweichow Moutai");
        CHINESE_TO_PINYIN.put("五粮液", "Wuliangye");
        CHINESE_TO_PINYIN.put("中国平安", "Ping An");
        CHINESE_TO_PINYIN.put("平安银行", "Ping An Bank");
        CHINESE_TO_PINYIN.put("招商银行", "CMBC");
        CHINESE_TO_PINYIN.put("食品饮料", "Food & Beverage");
        CHINESE_TO_PINYIN.put("买入", "BUY");
        CHINESE_TO_PINYIN.put("持有", "HOLD");
        CHINESE_TO_PINYIN.put("观望", "WATCH");
        CHINESE_TO_PINYIN.put("低风险", "Low Risk");
        CHINESE_TO_PINYIN.put("中等风险", "Medium Risk");
        CHINESE_TO_PINYIN.put("高风险", "High Risk");
        CHINESE_TO_PINYIN.put("技术面偏多", "Tech Bullish");
        CHINESE_TO_PINYIN.put("技术面偏空", "Tech Bearish");
        CHINESE_TO_PINYIN.put("技术面中性", "Tech Neutral");
        CHINESE_TO_PINYIN.put("积极", "Positive");
        CHINESE_TO_PINYIN.put("消极", "Negative");
        CHINESE_TO_PINYIN.put("中性", "Neutral");
        CHINESE_TO_PINYIN.put("超买区域，考虑卖出", "Overbought - Consider Selling");
        CHINESE_TO_PINYIN.put("超卖区域，考虑买入", "Oversold - Consider Buying");
        CHINESE_TO_PINYIN.put("中性区域，持有", "Neutral Zone - Hold");
        CHINESE_TO_PINYIN.put("综合技术面", "Combined Tech Analysis");
        CHINESE_TO_PINYIN.put("舆情", "Sentiment");
        CHINESE_TO_PINYIN.put("（", "(");
        CHINESE_TO_PINYIN.put("）", ")");
    }
    
    private String translateChinese(String text) {
        if (text == null) return "";
        
        String result = text;
        
        for (Map.Entry<String, String> entry : CHINESE_TO_PINYIN.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        
        result = result.replaceAll("[\\u4e00-\\u9fa5]", "");
        result = result.replaceAll("[\\uFF00-\\uFFEF]", "");
        result = result.replaceAll("[^\\x00-\\x7F]", "");
        result = result.trim().replaceAll("\\s+", " ");
        
        return result;
    }
    
    public byte[] generateReport(AnalysisResult result) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            PDType1Font font = PDType1Font.HELVETICA;
            PDType1Font boldFont = PDType1Font.HELVETICA_BOLD;
            
            float yPos = 700;
            
            contentStream.setFont(boldFont, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Stock Analysis Report");
            contentStream.endText();
            
            yPos -= 25;
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Stock: " + translateChinese(result.getStockName()) + " (" + result.getStockCode() + ")");
            contentStream.endText();
            
            yPos -= 30;
            
            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Investment Recommendation");
            contentStream.endText();
            
            yPos -= 20;
            contentStream.setFont(boldFont, 24);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText(translateChinese(result.getInvestment().getRecommendation()));
            contentStream.endText();
            
            yPos -= 25;
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Risk Level: " + translateChinese(result.getInvestment().getRiskLevel()));
            contentStream.endText();
            
            yPos -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Target Price: " + result.getInvestment().getTargetPrice());
            contentStream.endText();
            
            yPos -= 30;
            
            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Technical Analysis");
            contentStream.endText();
            
            yPos -= 20;
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("RSI (14): " + String.format("%.2f", result.getTechAnalyst().getRsi().getRsi()));
            contentStream.endText();
            
            yPos -= 18;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("MACD: " + translateChinese(result.getTechAnalyst().getRecommendation()));
            contentStream.endText();
            
            yPos -= 18;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("RSI Suggestion: " + translateChinese(result.getTechAnalyst().getRsi().getSuggestion()));
            contentStream.endText();
            
            yPos -= 30;
            
            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Sentiment Analysis");
            contentStream.endText();
            
            yPos -= 20;
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Overall: " + translateChinese(result.getSentiment().getOverallSentiment()));
            contentStream.endText();
            
            yPos -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Recent News:");
            contentStream.endText();
            
            yPos -= 18;
            for (String news : result.getSentiment().getNews()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(60, yPos);
                contentStream.showText("- " + translateChinese(news));
                contentStream.endText();
                yPos -= 18;
            }
            
            yPos -= 30;
            contentStream.setFont(font, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Reason: " + translateChinese(result.getInvestment().getReason()));
            contentStream.endText();
            
            contentStream.setFont(font, 9);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 30);
            contentStream.showText("Generated by Stock Analysis System MVP | Date: " + java.time.LocalDate.now());
            contentStream.endText();
            
            contentStream.close();
            document.save(baos);
        } catch (IOException e) {
            throw new RuntimeException("PDF generation failed", e);
        }
        
        return baos.toByteArray();
    }
}
