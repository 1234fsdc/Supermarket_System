package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品分页查询DTO
 */
@Data
public class ProductPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页条数
     */
    private int pageSize;

    /**
     * 商品名称（模糊搜索）
     */
    private String name;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 商品状态：0-停售，1-起售
     */
    private Integer status;
}
