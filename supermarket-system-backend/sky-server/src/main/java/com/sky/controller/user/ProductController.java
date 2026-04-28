package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Product;
import com.sky.result.Result;
import com.sky.service.ProductService;
import com.sky.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端商品控制器
 */
@Slf4j
@RestController("userProductController")
@RequestMapping("/user/product")
@Api(tags = "用户端-商品浏览接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询商品列表")
    public Result<List<ProductVO>> list(Long categoryId) {
        log.info("根据分类ID查询商品列表，分类ID：{}", categoryId);

        // 构造 Redis 缓存键：product_分类ID
        String key = "product_" + categoryId;

        // 从 Redis 中查询缓存数据
        List<ProductVO> list = (List<ProductVO>) redisTemplate.opsForValue().get(key);

        // 如果缓存中存在数据，直接返回
        if (list != null && !list.isEmpty()) {
            log.info("从 Redis 缓存中获取商品列表");
            return Result.success(list);
        }

        // 缓存中无数据，查询数据库
        Product product = Product.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        list = productService.listWithDetails(product);

        // 将查询结果存入 Redis 缓存
        redisTemplate.opsForValue().set(key, list);
        log.info("商品列表已存入 Redis 缓存");

        return Result.success(list);
    }

    /**
     * 根据ID查询商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/detail")
    @ApiOperation("根据ID查询商品详情")
    public Result<ProductVO> detail(@RequestParam Long id) {
        log.info("根据ID查询商品详情，商品ID：{}", id);
        ProductVO productVO = productService.getById(id);
        if (productVO == null || productVO.getStatus() != StatusConstant.ENABLE) {
            return Result.success(null);
        }
        return Result.success(productVO);
    }
}
