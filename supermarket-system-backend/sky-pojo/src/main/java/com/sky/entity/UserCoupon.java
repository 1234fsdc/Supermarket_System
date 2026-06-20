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
 * 用户优惠券实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态：1未使用 2已使用 3已过期 4已作废
     */
    public static final Integer STATUS_UNUSED = 1;
    public static final Integer STATUS_USED = 2;
    public static final Integer STATUS_EXPIRED = 3;
    public static final Integer STATUS_INVALID = 4;

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券模板ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券类型
     */
    private Integer couponType;

    /**
     * 优惠值
     */
    private BigDecimal discountValue;

    /**
     * 最低消费
     */
    private BigDecimal minSpend;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;

    /**
     * 使用时间
     */
    private LocalDateTime useTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 使用的订单ID
     */
    private Long orderId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
