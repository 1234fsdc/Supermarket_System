package com.sky.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
    /**
     * 根据菜品id列表查询套餐id列表
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishId(List<Long> dishIds);
    /**
     * 新增套餐关联的菜品数据
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
    /**
     * 根据套餐id删除套餐关联的菜品数据
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
    /**
     * 根据套餐id查询套餐关联的菜品数据
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
