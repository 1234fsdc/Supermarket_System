package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀订单DTO
 * 用于Redis队列传递异步下单任务
 */
@Data
public class SeckillOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 秒杀商品ID */
    private Long productId;

    /** 秒杀价格 */
    private BigDecimal seckillPrice;

    /** 购买数量 */
    private Integer quantity;
}
