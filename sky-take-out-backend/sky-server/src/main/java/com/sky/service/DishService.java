package com.sky.service;

import java.util.List;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

public interface DishService {
    /**
     * 新增菜品和口味数据
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
    /**
     * 菜品批量删除
     * ids就是想要删除的菜品id
     */
    public void deleteBatch(List<Long> ids);
    /**
     * 根据id查询菜品和口味数据
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id);
    /**
     * 修改菜品和口味数据
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO);
    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<DishVO> getByCategoryId(Long categoryId);
    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish);
    /**
     * 菜品的起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id);
}
