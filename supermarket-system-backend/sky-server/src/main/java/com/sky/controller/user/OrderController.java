package com.sky.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户端订单控制器
 *
 * 为什么创建这个类：
 * - 处理用户端的订单相关请求
 * - 提供订单的增删改查接口
 * - 支持订单支付、取消、再来一单等功能
 *
 * 怎么做的：
 * - 使用@RestController标记为REST控制器
 * - 注入OrderService处理订单业务逻辑
 * - 定义标准的RESTful API接口
 */
@RestController("userOrderController")  // 指定bean名称，避免与admin的OrderController冲突
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    /**
     * 订单服务
     * 为什么：处理订单相关的业务逻辑
     * 怎么做的：使用@Autowired自动注入
     */
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单接口
     *
     * 为什么：用户需要提交订单购买商品
     * 怎么做的：
     * - 接收订单提交DTO
     * - 调用OrderService创建订单
     * - 返回订单提交结果
     *
     * @param ordersSubmitDTO 订单提交信息，包含地址ID、商品列表等
     * @return Result<OrderSubmitVO> 订单提交结果，包含订单ID、订单号、金额等
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付接口（模拟）
     *
     * 为什么：用户需要支付订单
     * 怎么做的：调用OrderService更新订单支付状态
     *
     * @param orderId 订单ID
     * @return Result 操作结果
     */
    @PostMapping("/payment")
    @ApiOperation("订单支付")
    public Result payment(@RequestBody Long orderId) {
        log.info("模拟订单支付，订单 ID: {}", orderId);
        orderService.payOrder(orderId);
        return Result.success();
    }

    /**
     * 历史订单查询接口
     *
     * 为什么：用户需要查看自己的历史订单
     * 怎么做的：
     * - 接收分页参数和状态筛选
     * - 调用OrderService查询订单列表
     * - 返回分页结果
     *
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @param status 订单状态（可选）：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return Result<PageResult> 分页订单列表
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(
            Integer page,
            Integer pageSize,
            @RequestParam(required = false) Integer status) {
        PageResult pageResult = orderService.getHistoryOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情接口
     *
     * 为什么：用户需要查看订单的详细信息
     * 怎么做的：
     * - 接收订单ID路径参数
     * - 调用OrderService查询订单详情
     * - 返回订单VO（包含订单信息和商品明细）
     *
     * @param id 订单ID
     * @return Result<OrderVO> 订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 用户取消订单接口
     *
     * 为什么：用户可能需要取消未完成的订单
     * 怎么做的：
     * - 接收订单ID路径参数
     * - 调用OrderService取消订单
     * - 只有待付款或待接单的订单可以取消
     *
     * @param id 订单ID
     * @return Result 操作结果
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success();
    }

    /**
     * 再来一单接口
     *
     * 为什么：用户可能想重新购买之前订单中的商品
     * 怎么做的：
     * - 接收订单ID路径参数
     * - 查询原订单的商品明细
     * - 将商品添加到购物车
     *
     * @param id 订单ID
     * @return Result 操作结果
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id) {
        orderService.repetitionOrder(id);
        return Result.success();
    }
}
