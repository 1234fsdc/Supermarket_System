package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券优惠计算结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDiscountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userCouponId;

    private String couponName;

    private BigDecimal originalAmount;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    private Boolean usable;

    private String reason;
}
