package com.sky.service;

import com.sky.dto.SeckillOrderDTO;
import com.sky.result.Result;

/**
 * 秒杀订单服务接口
 */
public interface SeckillOrderService {

    /**
     * 秒杀抢购
     * @param productId 秒杀商品ID
     * @return 抢购结果
     */
    Result<String> seckill(Long productId);

    /**
     * 查询秒杀结果
     * @param productId 秒杀商品ID
     * @return 订单号（成功）/ null（排队中）/ 空字符串（失败）
     */
    Result<String> getSeckillResult(Long productId);
}
