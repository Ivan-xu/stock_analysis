package com.stock.analysis.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@Slf4j
@Component
public class ZhipuLLMGateway implements LLMGateway {

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String MODEL_NAME = "glm-4";
    private static final int TIMEOUT_MS = 30000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${zhipuai.api-key:}")
    private String apiKey;

    public ZhipuLLMGateway() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String query(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("智谱AI API Key未配置，返回降级响应");
            return getFallbackResponse(prompt);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);

            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("调用智谱AI API，提示词长度: {}", prompt.length());
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode choicesNode = rootNode.path("choices");

                if (choicesNode.isArray() && choicesNode.size() > 0) {
                    JsonNode messageNode = choicesNode.get(0).path("message");
                    String content = messageNode.path("content").asText();
                    log.info("智谱AI API调用成功，响应长度: {}", content.length());
                    return content;
                }
            }

            log.warn("智谱AI API响应格式异常，返回降级响应");
            return getFallbackResponse(prompt);

        } catch (HttpStatusCodeException e) {
            log.error("智谱AI API HTTP错误: 状态码={}, 响应={}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            return getFallbackResponse(prompt);
        } catch (Exception e) {
            log.error("智谱AI API调用异常: {}", e.getMessage(), e);
            return getFallbackResponse(prompt);
        }
    }

    @Override
    public boolean isAvailable() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.debug("智谱AI API Key未配置");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "hi");
            messages.add(message);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 10);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            boolean available = response.getStatusCode() == HttpStatus.OK;
            log.debug("智谱AI API可用性检查结果: {}", available);
            return available;

        } catch (Exception e) {
            log.debug("智谱AI API不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    private String getFallbackResponse(String prompt) {
        log.info("返回智谱AI降级响应");
        return "抱歉，当前AI服务暂时不可用。请稍后再试或联系管理员。\n" +
               "原始提示词: " + (prompt.length() > 50 ? prompt.substring(0, 50) + "..." : prompt);
    }
}
