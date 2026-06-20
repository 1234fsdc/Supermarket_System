package com.sky.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品实体类
 *
 * 为什么创建这个类：
 * - 定义参与秒杀的商品信息
 * - 独立控制秒杀库存（与正常库存分离）
 * - 记录秒杀价格和限购数量
 *
 * 怎么做的：
 * - 关联活动ID和商品ID
 * - 秒杀库存独立管理，防止影响正常销售
 * - 使用乐观锁防止超卖
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("seckill_product")
public class SeckillProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态常量
     */
    public static final Integer STATUS_DISABLED = 0; // 停用
    public static final Integer STATUS_ENABLED = 1;  // 启用

    private Long id;

    /** 活动ID */
    private Long activityId;

    /** 商品ID */
    private Long productId;

    /** 秒杀价格 */
    private BigDecimal seckillPrice;

    /** 秒杀库存 */
    private Integer seckillStock;

    /** 已售数量 */
    private Integer soldCount;

    /** 每人限购数量 */
    private Integer limitPerUser;

    /** 状态：0停用 1启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
