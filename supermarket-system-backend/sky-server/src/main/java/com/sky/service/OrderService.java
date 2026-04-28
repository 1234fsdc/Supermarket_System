package com.sky.service;

import com.sky.dto.OrderSubmitDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 模拟订单支付
     * @param orderId
     * @return
     */
    void payOrder(Long orderId);

    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    PageResult getHistoryOrders(Integer page, Integer pageSize, Integer status);
    
    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderDetail(Long id);
    /**
     * 用户取消订单
     * @param id
     */
       void cancelOrder(Long id);
    /**
     * 根据订单id再来一单
     * @param id
     */
    void repetitionOrder(Long id);
    /**
     * 管理端订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();
    /**
     * 接单
     * @param ordersConfirmDTO
     */
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);
    /**
     * 拒单
     * @param ordersConfirmDTO
     */
    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);
    /**
     * 管理端取消订单
     * @param id
     */
    void adminCancelOrder(OrdersCancelDTO ordersCancelDTO);
    /**
     * 派送订单
     * @param id
     */
    void deliveryOrder(Long id);
    /**
     * 完成订单
     * @param id
     */
    void completeOrder(Long id);

}
