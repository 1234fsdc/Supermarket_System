package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.context.BaseContext;
import com.sky.dto.SeckillOrderDTO;
import com.sky.entity.SeckillProduct;
import com.sky.exception.BaseException;
import com.sky.mapper.SeckillProductMapper;
import com.sky.result.Result;
import com.sky.service.SeckillOrderService;
import com.sky.vo.SeckillProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单服务实现类
 *
 * 核心设计：
 * 1. Redis 预减库存：使用 Lua 脚本保证原子性
 * 2. 用户限购检查：Redis 记录用户已购数量
 * 3. 异步下单：扣减成功后发送 MQ，异步创建订单
 * 4. 库存同步：活动开始时将数据库库存预热到 Redis
 */
@Slf4j
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    /** Redis 秒杀库存前缀 */
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    /** Redis 用户已购数量前缀 */
    private static final String SECKILL_USER_KEY = "seckill:user:";
    /** Redis 秒杀结果前缀 */
    private static final String SECKILL_RESULT_KEY = "seckill:result:";
    /** Redis 商品信息前缀 */
    private static final String SECKILL_PRODUCT_KEY = "seckill:product:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    /**
     * Lua 脚本：原子扣减库存
     * 参数：KEYS[1]=stockKey, KEYS[2]=userKey, ARGV[1]=limitPerUser
     * 返回：
     *   1  = 扣减成功
     *   0  = 库存不足
     *   -1 = 超过限购
     */
    private static final String STOCK_DEDUCT_LUA =
            "local stock = redis.call('get', KEYS[1]); " +
            "if stock == false or tonumber(stock) <= 0 then " +
            "    return 0; " +
            "end; " +
            "local userCount = redis.call('get', KEYS[2]); " +
            "if userCount ~= false and tonumber(userCount) >= tonumber(ARGV[1]) then " +
            "    return -1; " +
            "end; " +
            "redis.call('decr', KEYS[1]); " +
            "redis.call('incr', KEYS[2]); " +
            "redis.call('expire', KEYS[2], 86400); " +
            "return 1;";

    /**
     * 秒杀抢购
     *
     * 流程：
     * 1. 参数校验
     * 2. 从 Redis 获取商品信息
     * 3. Lua 脚本原子扣减库存
     * 4. 扣减成功 → 异步创建订单
     * 5. 返回抢购结果
     */
    @Override
    public Result<String> seckill(Long productId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户{}发起秒杀请求，商品ID：{}", userId, productId);

        // 1. 从 Redis 获取商品信息
        String productKey = SECKILL_PRODUCT_KEY + productId;
        SeckillProductVO product = (SeckillProductVO) redisTemplate.opsForValue().get(productKey);

        if (product == null) {
            // Redis 中没有，从数据库加载并预热
            product = loadProductToRedis(productId);
            if (product == null) {
                return Result.error("商品不存在或已下架");
            }
        }

        // 2. 检查活动是否有效
        if (product.getStatus() == null || product.getStatus() != 1) {
            return Result.error("商品已下架");
        }

        // 3. 检查库存
        String stockKey = SECKILL_STOCK_KEY + productId;
        Object stockObj = redisTemplate.opsForValue().get(stockKey);
        if (stockObj == null || Integer.parseInt(stockObj.toString()) <= 0) {
            return Result.error("商品已售罄");
        }

        // 4. Lua 脚本原子扣减库存
        String userKey = SECKILL_USER_KEY + productId + ":" + userId;
        Integer limitPerUser = product.getLimitPerUser() != null ? product.getLimitPerUser() : 1;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(STOCK_DEDUCT_LUA);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(stockKey),
                userKey,
                limitPerUser.toString()
        );

        // 处理 Lua 脚本执行结果
        if (result == null) {
            log.error("Redis Lua 脚本执行失败");
            return Result.error("系统繁忙，请稍后重试");
        }

        if (result == 0) {
            log.info("用户{}秒杀失败，商品{}库存不足", userId, productId);
            return Result.error("商品已售罄");
        }

        if (result == -1) {
            log.info("用户{}秒杀失败，商品{}超过限购数量", userId, productId);
            return Result.error("已超过限购数量");
        }

        // 5. 扣减成功，异步创建订单
        log.info("用户{}秒杀成功，商品{}，准备异步创建订单", userId, productId);

        // 生成唯一订单号
        String orderNo = generateOrderNo(userId, productId);

        // 将订单创建任务放入 Redis 队列（使用 List 模拟消息队列）
        SeckillOrderDTO orderDTO = new SeckillOrderDTO();
        orderDTO.setOrderNo(orderNo);
        orderDTO.setUserId(userId);
        orderDTO.setProductId(productId);
        orderDTO.setSeckillPrice(product.getSeckillPrice());
        orderDTO.setQuantity(1);

        String queueKey = "seckill:order:queue";
        redisTemplate.opsForList().leftPush(queueKey, JSON.toJSONString(orderDTO));

        // 记录秒杀结果（排队中）
        String resultKey = SECKILL_RESULT_KEY + userId + ":" + productId;
        redisTemplate.opsForValue().set(resultKey, "PENDING:" + orderNo, 5, TimeUnit.MINUTES);

        return Result.success(orderNo);
    }

    /**
     * 查询秒杀结果
     */
    @Override
    public Result<String> getSeckillResult(Long productId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        String resultKey = SECKILL_RESULT_KEY + userId + ":" + productId;
        Object result = redisTemplate.opsForValue().get(resultKey);

        if (result == null) {
            // 没有记录，说明未参与秒杀或已过期
            return Result.success("");
        }

        String resultStr = result.toString();
        if (resultStr.startsWith("PENDING:")) {
            // 排队中，返回订单号
            return Result.success(resultStr.substring(8));
        }
        if (resultStr.startsWith("SUCCESS:")) {
            // 成功，返回真实订单号
            return Result.success(resultStr.substring(8));
        }
        if (resultStr.equals("FAILED")) {
            return Result.success("");
        }

        return Result.success(resultStr);
    }

    /**
     * 从数据库加载商品信息到 Redis（库存预热）
     */
    private SeckillProductVO loadProductToRedis(Long productId) {
        SeckillProduct product = seckillProductMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            return null;
        }

        // 查询商品详细信息
        SeckillProductVO vo = seckillProductMapper.selectByIdWithProduct(productId);
        if (vo == null) {
            return null;
        }

        // 预热到 Redis
        String productKey = SECKILL_PRODUCT_KEY + productId;
        String stockKey = SECKILL_STOCK_KEY + productId;

        redisTemplate.opsForValue().set(productKey, vo, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(stockKey, product.getSeckillStock(), 1, TimeUnit.DAYS);

        log.info("商品{}库存预热完成，库存：{}", productId, product.getSeckillStock());
        return vo;
    }

    /**
     * 生成唯一订单号
     */
    private String generateOrderNo(Long userId, Long productId) {
        return "SK" + System.currentTimeMillis() + userId + productId +
               UUID.randomUUID().toString().substring(0, 4);
    }
}
