package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀商品VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long activityId;

    private Long productId;

    private String productName;

    private String productImage;

    private BigDecimal originalPrice;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer soldCount;

    private Integer limitPerUser;

    private Integer status;

    private Double discount; // 折扣率

    public Double getDiscount() {
        if (originalPrice == null || seckillPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return seckillPrice.divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
