package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long couponId;

    private String couponName;

    private Integer couponType;

    private String typeName;

    private BigDecimal discountValue;

    private BigDecimal minSpend;

    private Integer status;

    private String statusName;

    private LocalDateTime receiveTime;

    private LocalDateTime useTime;

    private LocalDateTime expireTime;

    private Long orderId;

    private Boolean isExpired;

    public String getTypeName() {
        if (couponType == null) return "";
        switch (couponType) {
            case 1: return "满减券";
            case 2: return "折扣券";
            case 3: return "新人券";
            default: return "未知";
        }
    }

    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case 1: return "未使用";
            case 2: return "已使用";
            case 3: return "已过期";
            case 4: return "已作废";
            default: return "未知";
        }
    }

    public Boolean getIsExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }
}
