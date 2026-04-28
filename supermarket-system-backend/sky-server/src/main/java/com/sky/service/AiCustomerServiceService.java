package com.sky.service;

/**
 * AI客服服务接口
 */
public interface AiCustomerServiceService {

    /**
     * 根据用户问题从知识库中检索答案
     *
     * @param question 用户问题
     * @return 匹配的答案
     */
    String getAnswer(String question);
}
