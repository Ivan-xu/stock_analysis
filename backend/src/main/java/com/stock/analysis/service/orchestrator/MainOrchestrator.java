package com.stock.analysis.service.orchestrator;

import com.stock.analysis.model.dto.*;
import com.stock.analysis.service.agent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主Agent编排器
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainOrchestrator {
    
    private final ResearcherAgent researcherAgent;
    private final TechAnalystAgent techAnalystAgent;
    private final SentimentAgent sentimentAgent;
    private final InvestmentManagerAgent investmentManagerAgent;
    
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    /**
     * 执行分析流水线 - SSE方式
     */
    public SseEmitter executePipelineSSE(String stockCode) {
        SseEmitter emitter = new SseEmitter(60000L);
        
        executor.submit(() -> {
            try {
                sendProgress(emitter, 1, "研究员Agent分析中...", 20);
                ResearcherResult research = researcherAgent.analyze(stockCode);
                Thread.sleep(500);
                
                sendProgress(emitter, 2, "技术分析师Agent分析中...", 40);
                TechResult tech = techAnalystAgent.analyze(research);
                Thread.sleep(500);
                
                sendProgress(emitter, 3, "舆情分析师Agent分析中...", 60);
                SentimentResult sentiment = sentimentAgent.analyze(stockCode);
                Thread.sleep(500);
                
                sendProgress(emitter, 4, "投资经理Agent决策中...", 80);
                InvestmentResult investment = investmentManagerAgent.decide(tech, sentiment);
                Thread.sleep(500);
                
                AnalysisResult result = AnalysisResult.builder()
                    .stockCode(stockCode)
                    .stockName(research.getStockName())
                    .researcher(research)
                    .techAnalyst(tech)
                    .sentiment(sentiment)
                    .investment(investment)
                    .build();
                
                sendComplete(emitter, result);
            } catch (Exception e) {
                sendError(emitter, e.getMessage());
            }
        });
        
        return emitter;
    }
    
    /**
     * 同步执行分析
     */
    public AnalysisResult executePipeline(String stockCode) {
        try {
            ResearcherResult research = researcherAgent.analyze(stockCode);
            TechResult tech = techAnalystAgent.analyze(research);
            SentimentResult sentiment = sentimentAgent.analyze(stockCode);
            InvestmentResult investment = investmentManagerAgent.decide(tech, sentiment);
            
            return AnalysisResult.builder()
                .stockCode(stockCode)
                .stockName(research.getStockName())
                .researcher(research)
                .techAnalyst(tech)
                .sentiment(sentiment)
                .investment(investment)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("分析失败", e);
        }
    }
    
    private void sendProgress(SseEmitter emitter, int step, String message, int progress) {
        try {
            emitter.send(SseEmitter.event()
                .name("progress")
                .data(Map.of(
                    "step", step,
                    "message", message,
                    "progress", progress
                )));
        } catch (Exception e) {
            log.warn("SSE发送失败", e);
        }
    }
    
    private void sendComplete(SseEmitter emitter, AnalysisResult result) {
        try {
            emitter.send(SseEmitter.event()
                .name("complete")
                .data(result));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
    
    private void sendError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                .name("error")
                .data(Map.of("message", message)));
        } catch (Exception e) {
            log.error("SSE发送错误失败", e);
        }
    }
}
