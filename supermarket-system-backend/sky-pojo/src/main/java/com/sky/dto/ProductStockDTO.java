package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商品库存变更DTO
 */
@Data
public class ProductStockDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 库存变更数量（正数为入库，负数为出库）
     */
    @NotNull(message = "变更数量不能为空")
    private Integer quantity;

    /**
     * 变更原因/备注
     */
    private String remark;
}
