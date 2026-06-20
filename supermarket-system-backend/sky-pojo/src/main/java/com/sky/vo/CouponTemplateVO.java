package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Integer type;

    private String typeName;

    private Integer discountType;

    private BigDecimal discountValue;

    private BigDecimal minSpend;

    private BigDecimal maxDiscount;

    private Integer totalCount;

    private Integer remainCount;

    private Integer limitPerUser;

    private Integer validDays;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String statusName;

    private Integer scopeType;

    private String scopeIds;

    private LocalDateTime createTime;

    public String getTypeName() {
        if (type == null) return "";
        switch (type) {
            case 1: return "满减券";
            case 2: return "折扣券";
            case 3: return "新人券";
            default: return "未知";
        }
    }

    public String getStatusName() {
        if (status == null) return "";
        return status == 1 ? "启用" : "停用";
    }
}
