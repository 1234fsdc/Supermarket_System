package com.sky.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * C端购物车控制器
 *
 * 为什么创建这个类：
 * - 处理用户购物车相关的HTTP请求
 * - 提供添加、查询、删除购物车商品的接口
 * - 支持用户下单前的商品管理
 *
 * 怎么做的：
 * - 使用@RestController标记为REST控制器
 * - 映射到"/user/shoppingCart"路径
 * - 注入ShoppingCartService处理业务逻辑
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端-用户购物车相关接口")
public class ShoppingCartController {

    /**
     * 购物车服务
     * 为什么：处理购物车相关的业务逻辑
     * 怎么做的：使用@Autowired自动注入
     */
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     *
     * 为什么：用户需要将商品加入购物车
     * 怎么做的：
     * - 使用@PostMapping映射POST请求
     * - 使用@RequestBody接收JSON格式的请求体
     * - 调用ShoppingCartService处理添加逻辑
     *
     * @param shoppingCartDTO 购物车商品信息，包含商品ID、口味等
     * @return Result 操作结果
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    // POST请求发送JSON数据时，需要在方法参数上加上@RequestBody注解
    // 为什么：告诉Spring将请求体中的JSON数据转换为Java对象
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车，商品信息为:{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查询购物车商品列表
     *
     * 为什么：用户需要查看购物车中的商品
     * 怎么做的：
     * - 使用@GetMapping映射GET请求
     * - 从ThreadLocal获取当前用户ID
     * - 查询该用户的购物车商品列表
     *
     * @return Result<List<ShoppingCart>> 购物车商品列表
     */
    @GetMapping("/list")
    @ApiOperation("查询购物车商品")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

    /**
     * 清空购物车
     *
     * 为什么：用户下单后或需要清空购物车
     * 怎么做的：
     * - 使用@DeleteMapping映射DELETE请求
     * - 删除当前用户的所有购物车商品
     *
     * @return Result 操作结果
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车商品")
    public Result clean() {
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    /**
     * 删除购物车中的一个商品
     *
     * 为什么：用户可能需要减少商品数量或删除某个商品
     * 怎么做的：
     * - 使用@PostMapping映射POST请求
     * - 接收商品信息
     * - 如果数量大于1则减1，等于1则删除
     *
     * @param shoppingCartDTO 要删除的商品信息
     * @return Result 操作结果
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车中一个商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车中一个商品，商品信息为:{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
