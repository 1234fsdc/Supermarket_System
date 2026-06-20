package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品销量排行VO
 *
 * 为什么创建这个类：
 * - 用于展示商品销量排行TOP10
 * - 包含商品基本信息和销量统计数据
 *
 * 实现思路：
 * - 从order_detail表统计每个商品的销售数量
 * - 关联product表获取商品名称、价格等信息
 * - 按销量降序排列，取前10名
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesRankVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String productImage;

    /** 商品分类ID */
    private Long categoryId;

    /** 商品单价 */
    private BigDecimal price;

    /** 销售数量 */
    private Integer salesCount;

    /** 销售金额 */
    private BigDecimal salesAmount;

    /** 排名 */
    private Integer rank;
}
