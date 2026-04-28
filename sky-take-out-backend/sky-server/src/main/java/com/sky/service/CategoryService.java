package com.sky.service;

import java.util.List;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

public interface CategoryService {
    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);
    /**
     * 启用/禁用分类
     * @param categoryDTO
     */
    void starOrStop(Integer status, Long id);
    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);
    /**
     * 根据id删除分类
     * @param id
     */
    void delete(Long id);
    /**
     * 根据类型查询分类
     * @return
     */
    List<Category> list(Integer type);
}
