package com.sky.service;

import java.util.List;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

public interface SetmealService {

    /**
     * 新增套餐
     * @param setmeal
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 分页查询套餐
     * @param setmealDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐，用于修改页面回显数据
     *
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);
    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
    /**
     * 根据分类id查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> listByCategoryId(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

}
