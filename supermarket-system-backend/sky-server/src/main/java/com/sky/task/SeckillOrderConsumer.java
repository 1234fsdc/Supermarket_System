package com.sky.task;

import com.alibaba.fastjson.JSON;
import com.sky.dto.SeckillOrderDTO;
import com.sky.mapper.SeckillProductMapper;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单异步消费者
 *
 * 使用 Redis List 作为简单消息队列，定时轮询处理秒杀订单
 * 生产环境建议替换为 RabbitMQ / RocketMQ
 */
@Slf4j
@Component
public class SeckillOrderConsumer {

    private static final String SECKILL_ORDER_QUEUE = "seckill:order:queue";
    private static final String SECKILL_RESULT_KEY = "seckill:result:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    /**
     * 定时消费秒杀订单队列
     * 每 1 秒执行一次
     */
    @Scheduled(fixedRate = 1000)
    public void consumeOrderQueue() {
        try {
            // 从队列右侧弹出订单（FIFO）
            Object orderObj = redisTemplate.opsForList().rightPop(SECKILL_ORDER_QUEUE, 1, TimeUnit.SECONDS);

            if (orderObj == null) {
                return; // 队列空，跳过
            }

            SeckillOrderDTO orderDTO = JSON.parseObject(orderObj.toString(), SeckillOrderDTO.class);
            log.info("消费秒杀订单：{}", orderDTO.getOrderNo());

            // 处理订单创建
            processOrder(orderDTO);

        } catch (Exception e) {
            log.error("消费秒杀订单异常", e);
        }
    }

    /**
     * 处理秒杀订单
     *
     * 流程：
     * 1. 数据库扣减库存（乐观锁）
     * 2. 创建订单记录
     * 3. 更新 Redis 结果状态
     */
    private void processOrder(SeckillOrderDTO orderDTO) {
        Long userId = orderDTO.getUserId();
        Long productId = orderDTO.getProductId();
        String orderNo = orderDTO.getOrderNo();

        try {
            // 1. 数据库扣减库存（使用乐观锁防止超卖）
            int affected = seckillProductMapper.deductStock(productId, orderDTO.getQuantity());

            if (affected <= 0) {
                // 库存不足，标记失败
                log.warn("订单{}数据库扣减库存失败，库存不足", orderNo);
                markResult(userId, productId, "FAILED", null);
                return;
            }

            // 2. TODO: 创建真实订单记录（orders 表）
            // 这里需要根据你的订单表结构来创建订单
            // 示例：orderService.createSeckillOrder(orderDTO);

            log.info("订单{}处理成功", orderNo);

            // 3. 标记成功
            markResult(userId, productId, "SUCCESS", orderNo);

        } catch (Exception e) {
            log.error("处理订单{}异常", orderNo, e);
            markResult(userId, productId, "FAILED", null);
        }
    }

    /**
     * 标记秒杀结果到 Redis
     */
    private void markResult(Long userId, Long productId, String status, String orderNo) {
        String resultKey = SECKILL_RESULT_KEY + userId + ":" + productId;
        String value;

        if ("SUCCESS".equals(status)) {
            value = "SUCCESS:" + orderNo;
        } else {
            value = "FAILED";
        }

        redisTemplate.opsForValue().set(resultKey, value, 10, TimeUnit.MINUTES);
    }
}
