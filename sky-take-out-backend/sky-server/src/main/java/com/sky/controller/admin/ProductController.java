package com.sky.controller.admin;

import com.sky.dto.ProductDTO;
import com.sky.dto.ProductPageQueryDTO;
import com.sky.entity.Product;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ProductService;
import com.sky.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 管理端商品控制器
 */
@Slf4j
@RestController("adminProductController")
@RequestMapping("/admin/product")
@Api(tags = "商品管理相关接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增商品
     *
     * @param productDTO 商品信息
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("新增商品")
    public Result<String> save(@RequestBody ProductDTO productDTO) {
        log.info("新增商品：{}", productDTO);
        productService.save(productDTO);

        // 清理 Redis 缓存
        clearCache();
        return Result.success();
    }

    /**
     * 商品分页查询
     *
     * @param productPageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("商品分页查询")
    public Result<PageResult> page(ProductPageQueryDTO productPageQueryDTO) {
        log.info("商品分页查询：{}", productPageQueryDTO);
        PageResult pageResult = productService.pageQuery(productPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 商品批量删除
     *
     * @param ids 商品ID列表
     * @return 操作结果
     */
    @DeleteMapping
    @ApiOperation("商品批量删除")
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("商品批量删除：{}", ids);
        productService.deleteBatch(ids);

        // 清理所有商品缓存
        clearCache();
        return Result.success();
    }

    /**
     * 根据ID查询商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询商品")
    public Result<ProductVO> getById(@PathVariable Long id) {
        log.info("根据ID查询商品：{}", id);
        ProductVO productVO = productService.getById(id);
        return Result.success(productVO);
    }

    /**
     * 修改商品信息
     *
     * @param productDTO 商品信息
     * @return 操作结果
     */
    @PutMapping
    @ApiOperation("修改商品")
    public Result<String> update(@RequestBody ProductDTO productDTO) {
        log.info("修改商品：{}", productDTO);
        productService.update(productDTO);

        // 清理 Redis 缓存
        clearCache();
        return Result.success();
    }

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询商品列表")
    public Result<List<ProductVO>> list(@RequestParam Long categoryId) {
        log.info("根据分类ID查询商品列表：{}", categoryId);
        List<ProductVO> list = productService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * 商品起售/停售
     *
     * @param status 状态：0-停售，1-起售
     * @param id     商品ID
     * @return 操作结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("商品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("商品起售停售，状态：{}，ID：{}", status, id);
        productService.startOrStop(status, id);

        // 清理 Redis 缓存
        clearCache();
        return Result.success();
    }

    /**
     * 清理商品相关的 Redis 缓存
     */
    private void clearCache() {
        Set<String> keys = redisTemplate.keys("product_*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("已清理商品缓存，数量：{}", keys.size());
        }
    }
}
