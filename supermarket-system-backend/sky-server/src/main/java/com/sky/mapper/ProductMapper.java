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

    /**
     * 扣减库存（乐观锁实现）
     * 为什么：防止超卖，保证并发安全
     * 怎么做的：使用版本号乐观锁，原子性更新
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @return 影响行数，大于0表示扣减成功
     */
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 恢复库存
     * 为什么：订单取消时恢复商品库存
     * 怎么做的：直接增加库存数量
     *
     * @param productId 商品ID
     * @param quantity  恢复数量
     * @return 影响行数
     */
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 查询商品库存（带行锁）
     * 为什么：获取最新库存并加锁，防止并发问题
     * 怎么做的：使用SELECT FOR UPDATE
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    Product selectByIdWithLock(Long productId);

    /**
     * 增加库存（乐观锁实现）
     *
     * @param productId 商品ID
     * @param quantity  增加数量
     * @param version   当前版本号
     * @return 影响行数
     */
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity, @Param("version") Integer version);

    /**
     * 根据关键词搜索商品（用户端）
     * 为什么：用户需要通过关键词查找商品
     * 怎么做的：模糊匹配商品名称和描述
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    @Select("SELECT * FROM product WHERE status = 1 AND (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) ORDER BY sales_volume DESC")
    List<Product> searchByKeyword(String keyword);

    /**
     * 减少库存（乐观锁实现）
     *
     * @param productId 商品ID
     * @param quantity  减少数量
     * @param version   当前版本号
     * @return 影响行数
     */
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity, @Param("version") Integer version);
}
