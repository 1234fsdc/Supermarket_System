package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SeckillProduct;
import com.sky.vo.SeckillProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 秒杀商品Mapper
 */
@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {

    /**
     * 根据活动ID查询秒杀商品列表（带商品信息）
     */
    List<SeckillProductVO> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 扣减秒杀库存
     */
    @Update("UPDATE seckill_product SET seckill_stock = seckill_stock - #{quantity}, sold_count = sold_count + #{quantity} " +
            "WHERE id = #{id} AND seckill_stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 根据活动ID和商品ID查询
     */
    @Select("SELECT * FROM seckill_product WHERE activity_id = #{activityId} AND product_id = #{productId} AND status = 1")
    SeckillProduct selectByActivityAndProduct(@Param("activityId") Long activityId, @Param("productId") Long productId);

    /**
     * 根据ID查询秒杀商品（带商品详细信息）
     */
    @Select("SELECT " +
            "  sp.id, sp.activity_id, sp.product_id, sp.seckill_price, " +
            "  sp.seckill_stock, sp.sold_count, sp.limit_per_user, sp.status, " +
            "  p.name AS product_name, p.image AS product_image, p.price AS original_price " +
            "FROM seckill_product sp " +
            "LEFT JOIN product p ON sp.product_id = p.id " +
            "WHERE sp.id = #{id}")
    SeckillProductVO selectByIdWithProduct(@Param("id") Long id);
}
