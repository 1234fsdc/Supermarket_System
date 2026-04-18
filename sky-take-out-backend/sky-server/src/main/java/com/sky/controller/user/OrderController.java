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

import com.sky.dto.OrderSubmitDTO;
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

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PostMapping("/payment")
    @ApiOperation("订单支付")
    public Result payment(@RequestBody Long orderId) {
        log.info("模拟订单支付，订单 ID: {}", orderId);
        orderService.payOrder(orderId);
        return Result.success();
    }
    
    /**
     * 历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     * - historyOrders 是历史订单查询接口 ，它查询的是 多个订单 ，而不是单个订单
        - PageResult 是分页结果封装类 ，用于封装分页查询的结果，包含：
         总记录数（total），当前页的数据列表（records），当前页码（page），每页记录数（pageSize）
        - OrderSubmitVO 是单个订单提交结果的封装 ，只包含单个订单的信息
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(Integer page, Integer pageSize, @RequestParam(required = false) Integer status){
        PageResult pageResult = orderService.getHistoryOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 根据订单id查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }
    /**
     * 用户取消订单
     *
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        orderService.cancelOrder(id);
        return Result.success();
    }

    /**
     * 根据订单id再来一单
     *
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        orderService.repetitionOrder(id);
        return Result.success();
    }
}