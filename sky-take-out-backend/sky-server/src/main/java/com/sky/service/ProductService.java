package com.sky.service;

import com.sky.dto.ProductDTO;
import com.sky.dto.ProductPageQueryDTO;
import com.sky.entity.Product;
import com.sky.result.PageResult;
import com.sky.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 新增商品
     *
     * @param productDTO 商品信息
     */
    void save(ProductDTO productDTO);

    /**
     * 商品分页查询（管理端）
     *
     * @param productPageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(ProductPageQueryDTO productPageQueryDTO);

    /**
     * 商品批量删除
     *
     * @param ids 商品ID列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据ID查询商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getById(Long id);

    /**
     * 修改商品信息
     *
     * @param productDTO 商品信息
     */
    void update(ProductDTO productDTO);

    /**
     * 根据分类ID查询商品列表（管理端）
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<ProductVO> getByCategoryId(Long categoryId);

    /**
     * 根据分类ID查询商品列表（用户端）
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<ProductVO> listByCategory(Long categoryId);

    /**
     * 根据条件查询商品列表（用户端）
     *
     * @param product 查询条件
     * @return 商品列表
     */
    List<ProductVO> listWithDetails(Product product);

    /**
     * 商品起售/停售
     *
     * @param status 状态：0-停售，1-起售
     * @param id     商品ID
     */
    void startOrStop(Integer status, Long id);
}
