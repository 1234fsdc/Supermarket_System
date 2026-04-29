package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.HttpClientUtil;
import com.sky.mapper.AddressBookMapper;
import java.math.BigDecimal;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * 为什么创建这个类：
 * - 实现订单相关的业务逻辑
 * - 处理订单的创建、查询、支付、取消等操作
 * - 管理订单状态流转
 *
 * 怎么做的：
 * - 实现OrderService接口
 * - 使用@Autowired注入Mapper和其他Service
 * - 使用@Transactional保证事务一致性
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    /**
     * 订单Mapper
     * 为什么：操作订单表
     * 怎么做的：使用@Autowired注入
     */
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 订单明细Mapper
     * 为什么：操作订单明细表
     */
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 购物车Mapper
     * 为什么：操作购物车表
     */
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 地址簿服务
     * 为什么：获取用户地址信息
     */
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 地址簿Mapper
     * 为什么：直接查询地址信息
     */
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 店铺地址配置
     * 为什么：用于计算配送距离
     * 怎么做的：从application.yml读取
     */
    @Value("${sky.shop.address}")
    private String shopAddress;

    /**
     * 百度地图AK
     * 为什么：调用百度地图API需要
     */
    @Value("${sky.baidu.ak}")
    private String ak;

    /**
     * 用户下单
     *
     * 为什么：用户提交购物车商品生成订单
     * 怎么做的：
     * 1. 校验地址和购物车
     * 2. 创建订单主表记录
     * 3. 创建订单明细记录
     * 4. 清空购物车
     * 5. 返回订单信息
     *
     * @param ordersSubmitDTO 订单提交信息
     * @return OrderSubmitVO 订单提交结果
     */
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 1. 校验地址簿
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 2. 查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 3. 向订单表插入一条数据
        Orders orders = new Orders();
        // 属性拷贝：将DTO中的属性复制到实体类
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.PAID);  // 模拟已支付
        orders.setStatus(Orders.TO_BE_CONFIRMED);  // 待接单状态
        orders.setCheckoutTime(LocalDateTime.now());
        // 使用时间戳生成订单号
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        // 组合完整地址
        String fullAddress = addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail();
        orders.setAddress(fullAddress);
        orders.setUserId(userId);
        orders.setAddressBookId(ordersSubmitDTO.getAddressBookId());

        orderMapper.insert(orders);

        // 4. 向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(cart.getName());
            orderDetail.setImage(cart.getImage());
            orderDetail.setNumber(cart.getNumber());
            orderDetail.setAmount(cart.getAmount());
            orderDetail.setProductId(cart.getProductId());
            orderDetail.setDishFlavor(cart.getDishFlavor());
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 5. 清空当前购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        // 6. 封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                // 设置金额精度为2位小数
                .orderAmount(orders.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP))
                .build();
        return orderSubmitVO;
    }

    /**
     * 模拟订单支付
     *
     * 为什么：更新订单支付状态
     * 怎么做的：更新订单的支付状态和状态
     *
     * @param orderId 订单ID
     */
    @Override
    public void payOrder(Long orderId) {
        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setPayStatus(Orders.PAID);
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 历史订单分页查询
     *
     * 为什么：用户查看自己的历史订单
     * 怎么做的：
     * 1. 使用MyBatis-Plus分页插件
     * 2. 查询订单列表
     * 3. 查询每个订单的商品明细
     * 4. 封装为VO返回
     *
     * @param page 当前页码
     * @param pageSize 每页条数
     * @param status 订单状态筛选
     * @return PageResult 分页结果
     */
    @Override
    public PageResult getHistoryOrders(Integer page, Integer pageSize, Integer status) {
        // 创建分页对象
        Page<Orders> pageResult = new Page<>(page, pageSize);

        // 构建查询条件
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        ordersPageQueryDTO.setUserId(userId);
        ordersPageQueryDTO.setStatus(status);

        // 执行分页查询
        IPage<Orders> ordersPage = orderMapper.pageQuery(pageResult, ordersPageQueryDTO);

        // 封装VO列表
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders order : ordersPage.getRecords()) {
            Long orderId = order.getId();
            // 查询订单明细
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderId);
            if (orderDetails == null) {
                orderDetails = new ArrayList<>();
            }
            // 封装VO
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            orderVOList.add(orderVO);
        }
        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    /**
     * 根据订单id查询订单详情
     *
     * 为什么：查看订单详细信息
     * 怎么做的：
     * 1. 查询订单主表
     * 2. 查询订单明细
     * 3. 封装为VO
     *
     * @param id 订单ID
     * @return OrderVO 订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders orders = orderMapper.selectById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * 为什么：用户取消未完成的订单
     * 怎么做的：
     * - 校验订单状态（只有待付款或待接单可以取消）
     * - 更新订单状态为已取消
     *
     * @param id 订单ID
     */
    @Override
    public void cancelOrder(Long id) {
        Orders orders = orderMapper.selectById(id);
        Integer ordersStatus = orders.getStatus();
        // 只有待付款或待接单可以取消
        if (ordersStatus != Orders.PENDING_PAYMENT && ordersStatus != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        } else {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("用户取消订单");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    /**
     * 根据订单id再来一单
     *
     * 为什么：快速重新购买之前订单的商品
     * 怎么做的：
     * 1. 查询原订单的商品明细
     * 2. 将商品添加到购物车
     *
     * @param id 订单ID
     */
    @Override
    public void repetitionOrder(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(id);
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 管理端订单搜索
     *
     * 为什么：管理员查看所有订单
     * 怎么做的：分页查询订单列表
     *
     * @param ordersPageQueryDTO 查询条件
     * @return PageResult 分页结果
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> pageResult = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        IPage<Orders> ordersPage = orderMapper.pageQuery(pageResult, ordersPageQueryDTO);
        List<OrderVO> orderVOList = getOrderVOList(ordersPage);
        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    /**
     * 获取订单VO列表
     *
     * 为什么：将订单实体转换为VO
     * 怎么做的：遍历订单列表，逐个转换
     *
     * @param ordersPage 订单分页结果
     * @return List<OrderVO> VO列表
     */
    private List<OrderVO> getOrderVOList(IPage<Orders> ordersPage) {
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders orders : ordersPage.getRecords()) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            // 获取订单商品信息字符串
            String orderProducts = getOrderProductsStr(orders);
            orderVO.setOrderDishes(orderProducts);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    /**
     * 获取订单商品信息字符串
     *
     * 为什么：在订单列表中显示商品概要
     * 怎么做的：
     * 1. 查询订单明细
     * 2. 格式化为"商品名*数量;"的字符串
     *
     * @param orders 订单实体
     * @return String 商品信息字符串
     */
    private String getOrderProductsStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(orders.getId());

        if (orderDetailList == null || orderDetailList.isEmpty()) {
            return "";
        }

        // 使用Stream API格式化商品信息
        List<String> orderProductList = orderDetailList.stream().map(x -> {
            String orderProduct = x.getName() + "*" + x.getNumber() + ";";
            return orderProduct;
        }).collect(Collectors.toList());

        return String.join("", orderProductList);
    }

    /**
     * 各个状态的订单数量统计
     *
     * 为什么：管理端显示待处理订单数量
     * 怎么做的：分别统计待接单、待派送、派送中的订单数
     *
     * @return OrderStatisticsVO 统计结果
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.getStatistics(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.getStatistics(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.getStatistics(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     *
     * 为什么：商家确认接单
     * 怎么做的：更新订单状态为已接单
     *
     * @param ordersConfirmDTO 接单信息
     */
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 拒单
     *
     * 为什么：商家拒绝接单
     * 怎么做的：
     * - 校验订单状态（必须是待接单）
     * - 更新状态为已取消，记录拒单原因
     *
     * @param ordersRejectionDTO 拒单信息
     */
    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.selectById(ordersRejectionDTO.getId());
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setId(ordersRejectionDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 管理端取消订单
     *
     * 为什么：管理员取消订单
     * 怎么做的：更新订单状态为已取消
     *
     * @param ordersCancelDTO 取消信息
     */
    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.selectById(ordersCancelDTO.getId());
        Integer ordersStatus = orders.getStatus();
        if (ordersStatus != Orders.PENDING_PAYMENT) {
            orders.setId(ordersCancelDTO.getId());
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("管理员取消订单");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    /**
     * 派送订单
     *
     * 为什么：商家开始配送
     * 怎么做的：将已接单状态改为派送中
     *
     * @param id 订单ID
     */
    @Override
    public void deliveryOrder(Long id) {
        Orders orders = orderMapper.selectById(id);
        Integer ordersStatus = orders.getStatus();
        if (orders != null && ordersStatus.equals(Orders.CONFIRMED)) {
            orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
            orderMapper.update(orders);
        }
    }

    /**
     * 完成订单
     *
     * 为什么：订单配送完成
     * 怎么做的：将派送中状态改为已完成，记录送达时间
     *
     * @param id 订单ID
     */
    @Override
    public void completeOrder(Long id) {
        Orders orders = orderMapper.selectById(id);
        Integer ordersStatus = orders.getStatus();
        if (orders != null && ordersStatus.equals(Orders.DELIVERY_IN_PROGRESS)) {
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    /**
     * 校验用户收货地址是否超出配送范围
     *
     * 为什么：确保在配送范围内才允许下单
     * 怎么做的：
     * 1. 调用百度地图API解析店铺地址坐标
     * 2. 调用百度地图API解析用户地址坐标
     * 3. 调用百度地图API计算路线距离
     * 4. 判断距离是否超过5000米
     *
     * @param address 用户收货地址
     */
    private void checkOutOfRange(String address) {
        // 1. 获取店铺坐标
        Map<String, String> map = new HashMap<>();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", ak);

        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        String shopLngLat = lat + "," + lng;

        // 2. 获取用户地址坐标
        map.put("address", address);
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("收货地址解析失败");
        }

        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        String userLngLat = lat + "," + lng;

        // 3. 计算路线距离
        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        // 4. 判断距离是否超过5000米
        if (distance > 5000) {
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
