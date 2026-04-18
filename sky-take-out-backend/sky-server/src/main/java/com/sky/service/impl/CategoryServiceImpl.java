package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;

import java.util.List;

import org.springframework.beans.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        /** 思路：
         * 创建 MyBatis-Plus 的分页对象，传入页码和每页记录数
         * 调用Mapper的pageQuery方法，传入分页对象和查询条件对象
         * 返回分页结果
         */
        Page<Category> page = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<Category> resultPage = categoryMapper.pageQuery(page, categoryPageQueryDTO);
        return new PageResult(resultPage.getTotal(), resultPage.getRecords());
    }
    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        /** 思路：
         * 转换为实体类，然后拷贝属性
         * 设置分类状态为禁用
         * 新增分类，利用Mapper的insert方法新增
         * 自动填充功能会自动设置 create_time、update_time、create_user、update_user
         */
        // 转换为实体类
        Category category = new Category();
        // 这行代码会自动将 CategoryDTO 中所有 同名同类型 的属性值复制到 Category 实体中。
        // 所以type、name、sort 这三个字段也已经通过 BeanUtils.copyProperties() 被正确设置了。
        BeanUtils.copyProperties(categoryDTO, category);
        // 设置分类状态
        category.setStatus(StatusConstant.DISABLE);
        // 新增分类
        categoryMapper.insert(category);
    }
    /**
     * 启用/禁用分类
     * @param categoryDTO
     */
    @Override
    public void starOrStop(Integer status, Long id) {
        /** 思路：
         * 创建一个新的对象后设置一个id,再设置状态，再将设置好的对象更新Mapper
         */
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryMapper.update(category);
    }
    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        /** 思路：
         * 转换为实体类，然后拷贝属性
         * 修改分类，利用Mapper的updateById方法更新
         * 自动填充功能会自动设置 update_time、update_user
         */
        // 转换为实体类
        Category category = new Category();
        // 这行代码会自动将 CategoryDTO 中所有 同名同类型 的属性值复制到 Category 实体中。
        // 所以type、name、sort 这三个字段也已经通过 BeanUtils.copyProperties() 被正确设置了。
        BeanUtils.copyProperties(categoryDTO, category);
        // 修改分类
        categoryMapper.updateById(category);
    }
    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void delete(Long id) {
         //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有套餐，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        // 根据id删除分类
        categoryMapper.deleteById(id);
    }
    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        // 根据类型查询分类
        return categoryMapper.list(type);
    }
}
