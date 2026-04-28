package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.ProductPageQueryDTO;
import com.sky.entity.Product;
import com.sky.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据分类ID查询商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    @Select("SELECT COUNT(id) FROM product WHERE category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 商品分页查询（管理端）
     *
     * @param page                分页参数
     * @param productPageQueryDTO 查询条件
     * @return 分页结果
     */
    Page<ProductVO> pageQuery(IPage<ProductVO> page, @Param("productPageQueryDTO") ProductPageQueryDTO productPageQueryDTO);

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<ProductVO> getByCategoryId(Long categoryId);

    /**
     * 根据分类ID和状态查询商品列表（用户端）
     *
     * @param categoryId 分类ID
     * @param status     状态
     * @return 商品列表
     */
    @Select("SELECT * FROM product WHERE category_id = #{categoryId} AND status = #{status} ORDER BY sales_volume DESC")
    List<Product> getByCategoryIdAndStatus(Long categoryId, Integer status);

    /**
     * 批量删除商品
     *
     * @param ids 商品ID列表
     */
    void deleteByIds(List<Long> ids);
}
