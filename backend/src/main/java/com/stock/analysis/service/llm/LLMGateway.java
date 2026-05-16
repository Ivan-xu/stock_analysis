package com.stock.analysis.service.llm;

/**
 * LLM网关接口
 */
public interface LLMGateway {
    
    /**
     * 同步查询
     * @param prompt 提示词
     * @return AI响应内容
     */
    String query(String prompt);
    
    /**
     * 检查LLM是否可用
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取当前模型名称
     * @return 模型名称
     */
    String getModelName();
}
