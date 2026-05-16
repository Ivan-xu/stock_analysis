package com.stock.analysis.controller;

import com.stock.analysis.model.dto.AnalysisResult;
import com.stock.analysis.service.orchestrator.MainOrchestrator;
import com.stock.analysis.service.report.PDFReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * 股票分析Controller
 */
@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StockAnalysisController {
    
    private final MainOrchestrator orchestrator;
    private final PDFReportService pdfReportService;
    
    /**
     * 同步分析
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody Map<String, String> request) {
        String stockCode = request.get("stockCode");
        AnalysisResult result = orchestrator.executePipeline(stockCode);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "result", result
        ));
    }
    
    /**
     * SSE流式分析
     */
    @GetMapping(value = "/stream/{stockCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeStream(@PathVariable String stockCode) {
        return orchestrator.executePipelineSSE(stockCode);
    }
    
    /**
     * 下载PDF报告
     */
    @GetMapping("/pdf/{stockCode}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String stockCode) {
        AnalysisResult result = orchestrator.executePipeline(stockCode);
        byte[] pdfBytes = pdfReportService.generateReport(result);
        
        String filename = String.format("stock_analysis_%s.pdf", stockCode);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}
