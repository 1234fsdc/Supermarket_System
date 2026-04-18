package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;


@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    /**
     * 新增套餐
     * @param setmeal
     */
    int insert(Setmeal setmeal);

    /**
     * 分页查询套餐
     * @param page
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(Page<SetmealVO> page, @Param("setmealPageQueryDTO") SetmealPageQueryDTO setmealPageQueryDTO);
    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 根据id删除套餐
     * @param setmealId
     */
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long setmealId);
    /**
     * 更新套餐
     * @param setmeal
     */
    void update(Setmeal setmeal);
    /**
     * 根据套餐 id 查询菜品
     * @param setmealId
     * @return
    */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 使用 QueryWrapper 分页查询套餐
     * @param page
     * @param queryWrapper
     * @return
     */
    Page<Setmeal> selectPage(Page<Setmeal> page, @Param("ew") QueryWrapper<Setmeal> queryWrapper);
    /**
     * 根据分类 id 查询套餐
     * @param categoryId
     * @param status
     * @return
     */
    @Select("select * from setmeal where category_id = #{categoryId} and status = #{status} order by create_time desc")
    List<Setmeal> listByCategoryId(Long categoryId, Integer status);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

}
