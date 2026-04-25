package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sky.entity.ShoppingCart;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 根据条件查询购物车数据
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id更新购物车数量
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     */
    @Insert("insert into shopping_cart (user_id, name, image, amount, number, create_time, product_id, dish_flavor) values (#{userId}, #{name}, #{image}, #{amount}, #{number}, #{createTime}, #{productId}, #{dishFlavor})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车数据
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 根据id删除购物车数据
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
}
