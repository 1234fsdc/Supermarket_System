package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.sky.entity.DishFlavor;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入菜品口味数据
     * @param flavors
     */
    
    void insertBatch(List<DishFlavor> flavors);
    /**
     * 根据菜品id删除菜品口味数据
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
    /**
     * 根据菜品id列表删除菜品口味数据
     * @param dishIds
     */
    @Delete("delete from dish_flavor where dish_id in (#{dishIds})")
    void deleteByDishIds(List<Long> dishIds);
    /**
     * 根据菜品id查询菜品口味数据
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
