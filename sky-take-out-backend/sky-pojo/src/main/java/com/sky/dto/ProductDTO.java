package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品DTO - 用于添加/修改商品
 */
@Data
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID（修改时必填）
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品分类名称
     */
    private String categoryName;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品状态：0-停售，1-起售
     */
    private Integer status;

    /**
     * 商品单位（如：支、袋、盒、瓶）
     */
    private String unit;

    /**
     * 促销标签（可选）
     */
    private String promoTag;
}
