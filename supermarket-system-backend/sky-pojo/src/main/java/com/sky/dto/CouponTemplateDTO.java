package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板DTO
 *
 * 为什么创建这个类：
 * - 用于创建/修改优惠券模板的数据传输
 * - 包含参数校验注解
 */
@Data
public class CouponTemplateDTO implements Serializable {

    private Long id;

    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    @NotNull(message = "优惠券类型不能为空")
    private Integer type;

    @NotNull(message = "优惠方式不能为空")
    private Integer discountType;

    @NotNull(message = "优惠值不能为空")
    @DecimalMin(value = "0.01", message = "优惠值必须大于0")
    private BigDecimal discountValue;

    @NotNull(message = "最低消费金额不能为空")
    @DecimalMin(value = "0.00", message = "最低消费金额不能为负数")
    private BigDecimal minSpend;

    private BigDecimal maxDiscount;

    @Min(value = 0, message = "发放总量不能为负数")
    private Integer totalCount;

    @Min(value = 1, message = "每人限领数量至少为1")
    private Integer limitPerUser;

    private Integer validDays;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    @NotNull(message = "适用范围不能为空")
    private Integer scopeType;

    private String scopeIds;
}
