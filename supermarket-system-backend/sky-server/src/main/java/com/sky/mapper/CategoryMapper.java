package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CategoryMapper extends BaseMapper<Category> {
    /**
     * 分类分页查询
     * @param page
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(IPage<Category> page, @Param("categoryPageQueryDTO") CategoryPageQueryDTO categoryPageQueryDTO);
    /**
     *  分类状态启用/禁用
     * @param category
     */
    void update(Category category);
    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    List<Category> list(Integer type);

    // @Insert("insert into category(type,name,description,create_user,update_user,create_time,update_time,status) values (#{type},#{name},#{description},#{createUser},#{updateUser},#{createTime},#{updateTime},#{status})")
    // void insert(Category category);

}