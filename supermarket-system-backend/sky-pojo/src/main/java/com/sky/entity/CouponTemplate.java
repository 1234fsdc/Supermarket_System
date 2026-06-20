package com.sky.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon_template")
public class CouponTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券类型：1满减券 2折扣券 3新人券
     */
    public static final Integer TYPE_FULL_REDUCTION = 1;
    public static final Integer TYPE_DISCOUNT = 2;
    public static final Integer TYPE_NEW_USER = 3;

    /**
     * 优惠方式：1固定金额 2百分比折扣
     */
    public static final Integer DISCOUNT_TYPE_FIXED = 1;
    public static final Integer DISCOUNT_TYPE_PERCENT = 2;

    /**
     * 适用范围：1全场通用 2指定分类 3指定商品
     */
    public static final Integer SCOPE_ALL = 1;
    public static final Integer SCOPE_CATEGORY = 2;
    public static final Integer SCOPE_PRODUCT = 3;

    /**
     * 状态：0停用 1启用
     */
    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 优惠券模板ID
     */
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 优惠券类型
     */
    private Integer type;

    /**
     * 优惠方式
     */
    private Integer discountType;

    /**
     * 优惠值
     */
    private BigDecimal discountValue;

    /**
     * 最低消费金额
     */
    private BigDecimal minSpend;

    /**
     * 最大优惠金额
     */
    private BigDecimal maxDiscount;

    /**
     * 发放总量
     */
    private Integer totalCount;

    /**
     * 剩余数量
     */
    private Integer remainCount;

    /**
     * 每人限领数量
     */
    private Integer limitPerUser;

    /**
     * 领取后有效天数
     */
    private Integer validDays;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 适用范围类型
     */
    private Integer scopeType;

    /**
     * 适用范围ID列表
     */
    private String scopeIds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
