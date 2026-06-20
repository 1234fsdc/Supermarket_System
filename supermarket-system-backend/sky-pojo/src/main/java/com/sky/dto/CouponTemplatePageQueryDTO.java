package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券模板分页查询DTO
 */
@Data
public class CouponTemplatePageQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer pageSize = 10;

    private String name;

    private Integer type;

    private Integer status;
}
