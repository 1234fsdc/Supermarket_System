package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    int insert(Dish dish);

    /**
     * 菜品分页查询
     * @param page
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(IPage<DishVO> page, @Param("dishPageQueryDTO") DishPageQueryDTO dishPageQueryDTO);
    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);
    /**
     * 根据id删除菜品
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);
    /**
     * 根据菜品id列表删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);
    /**
     * 根据id修改菜品表基本信息
     * @param dish
     */
    void update(Dish dish);
    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<DishVO> getByCategoryId(Dish dish);
    /**
     * 根据套餐id查询套餐关联的菜品数据
     * @param id
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);
    /**
     * 根据查询条件查询菜品
     * @param dish
     * @return
     */ 
    @Select("select * from dish where status = #{status} and (name like concat('%',#{name},'%') or category_id = #{categoryId})")
    List<Dish> list(Dish dish);
}
