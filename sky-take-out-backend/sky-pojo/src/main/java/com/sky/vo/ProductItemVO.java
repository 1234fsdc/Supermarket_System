package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品明细项VO - 用于订单/购物车商品项展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer copies;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 商品描述
     */
    private String description;
}
