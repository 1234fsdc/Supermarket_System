package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.ProductDTO;
import com.sky.dto.ProductPageQueryDTO;
import com.sky.entity.Product;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.ProductMapper;
import com.sky.result.PageResult;
import com.sky.service.ProductService;
import com.sky.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 新增商品
     *
     * @param productDTO 商品信息
     */
    @Override
    @Transactional
    public void save(ProductDTO productDTO) {
        log.info("新增商品：{}", productDTO.getName());

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        // 初始化销量、好评率等数据
        product.setSalesVolume(0);
        product.setRating(100);
        product.setRebuyCount(0);

        productMapper.insert(product);
    }

    /**
     * 商品分页查询（管理端）
     *
     * @param productPageQueryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(ProductPageQueryDTO productPageQueryDTO) {
        log.info("商品分页查询，页码：{}，每页条数：{}", productPageQueryDTO.getPage(), productPageQueryDTO.getPageSize());

        Page<ProductVO> page = new Page<>(productPageQueryDTO.getPage(), productPageQueryDTO.getPageSize());
        Page<ProductVO> pageResult = productMapper.pageQuery(page, productPageQueryDTO);

        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
    }

    /**
     * 商品批量删除
     *
     * @param ids 商品ID列表
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        log.info("批量删除商品，ID列表：{}", ids);

        // 检查是否有起售中的商品
        for (Long id : ids) {
            Product product = productMapper.selectById(id);
            if (product != null && product.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.PRODUCT_ON_SALE);
            }
        }

        // 批量删除商品
        productMapper.deleteByIds(ids);
    }

    /**
     * 根据ID查询商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @Override
    public ProductVO getById(Long id) {
        log.info("根据ID查询商品：{}", id);

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new DeletionNotAllowedException(MessageConstant.PRODUCT_NOT_FOUND);
        }

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);
        return productVO;
    }

    /**
     * 修改商品信息
     *
     * @param productDTO 商品信息
     */
    @Override
    @Transactional
    public void update(ProductDTO productDTO) {
        log.info("修改商品：{}", productDTO.getName());

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        productMapper.updateById(product);
    }

    /**
     * 根据分类ID查询商品列表（管理端）
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @Override
    public List<ProductVO> getByCategoryId(Long categoryId) {
        log.info("根据分类ID查询商品列表，分类ID：{}", categoryId);
        return productMapper.getByCategoryId(categoryId);
    }

    /**
     * 根据分类ID查询商品列表（用户端）
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @Override
    public List<ProductVO> listByCategory(Long categoryId) {
        log.info("根据分类ID查询商品列表（用户端），分类ID：{}", categoryId);

        List<Product> productList = productMapper.getByCategoryIdAndStatus(categoryId, StatusConstant.ENABLE);

        List<ProductVO> productVOList = new ArrayList<>();
        for (Product product : productList) {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(product, productVO);
            productVOList.add(productVO);
        }

        return productVOList;
    }

    /**
     * 根据条件查询商品列表（用户端）
     *
     * @param product 查询条件
     * @return 商品列表
     */
    @Override
    public List<ProductVO> listWithDetails(Product product) {
        log.info("根据条件查询商品列表，分类ID：{}", product.getCategoryId());

        List<Product> productList = productMapper.getByCategoryIdAndStatus(product.getCategoryId(), StatusConstant.ENABLE);

        List<ProductVO> productVOList = new ArrayList<>();
        for (Product p : productList) {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(p, productVO);
            productVOList.add(productVO);
        }

        return productVOList;
    }

    /**
     * 商品起售/停售
     *
     * @param status 状态：0-停售，1-起售
     * @param id     商品ID
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        log.info("商品起售/停售，ID：{}，状态：{}", id, status);

        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new DeletionNotAllowedException(MessageConstant.PRODUCT_NOT_FOUND);
        }

        product.setStatus(status);
        productMapper.updateById(product);
    }
}
