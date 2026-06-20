package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀商品DTO
 */
@Data
public class SeckillProductDTO implements Serializable {

    private Long id;

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价格必须大于0")
    private BigDecimal seckillPrice;

    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 1, message = "秒杀库存至少为1")
    private Integer seckillStock;

    @Min(value = 1, message = "每人限购至少为1")
    private Integer limitPerUser;
}
