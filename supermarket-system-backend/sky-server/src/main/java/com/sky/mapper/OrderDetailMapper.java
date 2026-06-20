package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import com.sky.vo.ProductSalesRankVO;


/**
 * 订单详情Mapper
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail>  {
    /**
     * 批量插入订单明细
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);
    /**
     * 根据订单 id 查询订单明细
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> selectList(@Param("orderId") Long orderId);

    /**
     * 查询商品销量排行TOP10
     * 实现思路：
     * 1. 从order_detail表统计每个商品的销售数量总和
     * 2. 关联product表获取商品名称、价格、图片等信息
     * 3. 只统计已完成的订单（orders.status = 5）
     * 4. 按销量降序排列，取前10名
     *
     * @return 商品销量排行列表
     */
    @Select("SELECT " +
            "od.product_id AS productId, " +
            "p.name AS productName, " +
            "p.image AS productImage, " +
            "p.category_id AS categoryId, " +
            "p.price AS price, " +
            "SUM(od.number) AS salesCount, " +
            "SUM(od.amount * od.number) AS salesAmount " +
            "FROM order_detail od " +
            "INNER JOIN product p ON od.product_id = p.id " +
            "INNER JOIN orders o ON od.order_id = o.id " +
            "WHERE o.status = 5 " +
            "GROUP BY od.product_id, p.name, p.image, p.category_id, p.price " +
            "ORDER BY salesCount DESC " +
            "LIMIT 10")
    List<ProductSalesRankVO> getProductSalesRankTop10();

}
