package com.sky.service;

import com.sky.dto.CustomerServiceResult;

/**
 * AI客服服务接口
 */
public interface AiCustomerServiceService {

    /**
     * 根据用户问题从知识库中检索答案
     *
     * @param question 用户问题
     * @return 匹配的答案（含推荐商品）
     */
    CustomerServiceResult getAnswer(String question);
}
