package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrderSubmitDTO;
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

import lombok.experimental.var;

import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.HttpClientUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;



@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private AddressBookMapper addressBookMapper;

    // 配置外卖商家店铺地址和百度地图的AK
    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.ak}")
    private String ak;

    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 暂时关闭配送范围检查（测试用）
        // checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            //抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.PAID); // 直接设置为已支付
        orders.setStatus(Orders.TO_BE_CONFIRMED); // 直接设置为待接单
        orders.setCheckoutTime(LocalDateTime.now()); // 设置支付时间
        //强转成字符串
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        //插入地址
        orders.setAddressBookId(ordersSubmitDTO.getAddressBookId());

        orderMapper.insert(orders);

        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart : shoppingCartList){
            // 将购物车数据封装成orderDetail对象
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(cart.getName());
            orderDetail.setImage(cart.getImage());
            orderDetail.setNumber(cart.getNumber());
            orderDetail.setAmount(cart.getAmount());
            orderDetail.setDishId(cart.getDishId());
            orderDetail.setSetmealId(cart.getSetmealId());
            orderDetail.setDishFlavor(cart.getDishFlavor());
            //设置当前订单明细关联的订单id
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空当前购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;

    }

    /**
     * 模拟订单支付
     * @param orderId
     */
    @Override
    public void payOrder(Long orderId) {
        // 模拟支付成功，直接更新订单状态为已支付
        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setPayStatus(Orders.PAID); // 设置为已支付
        orders.setStatus(Orders.TO_BE_CONFIRMED); // 设置为待确认
        orders.setCheckoutTime(LocalDateTime.now()); // 设置支付时间
        orderMapper.update(orders);
    }

    /**
     * 历史订单分页查询
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */ 
    @Override
    public PageResult getHistoryOrders(Integer page, Integer pageSize, Integer status) {
        /**
         * 思路
         * 1. 分页查询订单表
         *    1.1 利用MP创建分页查询对象，并设置当前页和每页条数
         *    1.2 封装一个DTO，设置用户id和订单状态，便于传给 Mapper 层去查数据库
         *    1.3 执行pageQuery分页查询（mapper 方法直接返回 IPage）
         * 2. 这时数据库查出来的 Orders，没有「订单里的菜品列表」！只有订单头信息！
         *    2.1 创建一个订单集合，来存放一个订单中的多个菜品
         *    2.2 获取userid来查询订单详细表中的订单明细
         *    2.3 利用Mapper获取详细的数据封装到VO中去，因为要返回VO
         *    2.4 将VO添加到订单集合中去
         * 3. 返回总记录数， 当前页数据
         */
        // 1. 创建分页对象（当前页，每页条数）
        Page<Orders> pageResult = new Page<>(page, pageSize);
        // 2. 封装查询条件
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        ordersPageQueryDTO.setUserId(userId);
        ordersPageQueryDTO.setStatus(status);
        // 3. 执行分页查询（mapper 方法直接返回 IPage）
        IPage<Orders> ordersPage = orderMapper.pageQuery(pageResult,ordersPageQueryDTO);
        // 数据库查出来的 Orders，没有「订单里的菜品列表」！只有订单头信息！
        List<OrderVO> orderVOList = new ArrayList<>();
        for(Orders order : ordersPage.getRecords()){
            // 获取订单 id
            Long orderId = order.getId();
            //获取订单详细信息，确保不为 null
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderId);
            if(orderDetails == null) {
                orderDetails = new ArrayList<>();
            }
            //封装 VO，因为返回值是 VO
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            //将一个订单 VO 添加到列表中
            orderVOList.add(orderVO);
        }
        return new PageResult(ordersPage.getTotal(), orderVOList);
    }
    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        /**
         * 思路
         * 1. 先根据订单id获取订单信息
         *    1.1 利用Mapper查询订单表中的订单
         * 2. 然后获取订单明细信息
         *    2.1 创建一个订单详细集合
         *    2.2 遍历订单明细集合，得到单个order
         *    2.3 然后将单个的order添加到集合中
         * 3. 封装VO，因为返回值是VO,
         * 4. 
         */
        Orders orders = orderMapper.selectById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 用户取消订单
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @param id
     */
    @Override
    public void cancelOrder(Long id) {
        /**
         * 思路：
         * 1. 先根据订单id查询订单信息
         *    1.1 利用Mapper查询订单表中的订单
         *    1.2 获取订单状态
         * 2. 如果订单状态是待付款，才允许取消订单
         *    2.1 判断订单状态是否是待付款
         *    2.2 如果是待付款，更新订单状态为已取消
         *    2.3 如果是待付款，抛出业务异常
         * 3. 更新订单状态、取消原因、取消时间
         */

        Orders orders = orderMapper.selectById(id);
        Integer ordersStatus = orders.getStatus();
        if(ordersStatus != Orders.PENDING_PAYMENT){
            //抛出业务异常
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }else{
            //更新订单状态为已取消
            orders.setStatus(Orders.CANCELLED);
            //更新取消原因
            orders.setCancelReason("用户取消订单");
            //更新取消时间
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }
    /**
     * 根据订单id再来一单
     * @param id
     */
    @Override
    public void repetitionOrder(Long id) {
        /**
         * 思路：
         * 1. 先根据订单id查询订单信息
         *    1.1 利用Mapper查询订单表中的订单
         * 2. 获取它的订单明细信息
         *    2.1 利用Mapper查询订单明细表返回一个List集合
         * 3. 根据订单明细信息，将菜品添加到购物车
         *    3.1 清空当前购物车数据
         *    3.2 遍历订单明细集合，得到单个orderDetail
         *    3.3 利用Mapper插入购物车表中的数据
         *    3.4 清空当前购物车数据
         */
        Orders orders = orderMapper.selectById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(id);
        for(OrderDetail orderDetail : orderDetailList){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCartMapper.insert(shoppingCart);
        }
    }
    /**
     * 管理端订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        /**
         * 思路：
        先设置分页：告诉数据库只查当前这一页的数据。
        根据条件去数据库查订单列表。
        查出来的订单只有基本信息，没有显示菜品名称那一行文字。
        所以我要循环每个订单，把订单里的菜品拼成一句话。
        把订单信息 + 菜品文字 打包成前端要的 OrderVO。
        最后把「总订单数 + 处理好的订单列表」返回给前端。
        */
        //先设置分页：告诉数据库只查当前这一页的数据。
       Page<Orders> pageResult = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
       //根据条件去数据库查订单列表。
       IPage<Orders> ordersPage = orderMapper.pageQuery(pageResult,ordersPageQueryDTO);
        // getOrderVOList把每个订单 → 加上菜品文字 → 变成前端能直接显示的 OrderVO
       List<OrderVO> orderVOList = getOrderVOList(ordersPage);
       return new PageResult(ordersPage.getTotal(), orderVOList);
    }
    /**
     * 获取订单菜品信息
     * @param orders
     * @return
     */
    private List<OrderVO> getOrderVOList(IPage<Orders> ordersPage){
        List<OrderVO> orderVOList = new ArrayList<>();
        for(Orders orders : ordersPage.getRecords()){
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            String orderDishes = getOrderDishesStr(orders);
            // 将订单菜品信息封装到orderVO中，并添加到orderVOList
            orderVO.setOrderDishes(orderDishes);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }
    /**
     * 获取订单菜品信息
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、已接单、派送中的订单数量
        Integer toBeConfirmed = orderMapper.getStatistics(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.getStatistics(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.getStatistics(Orders.DELIVERY_IN_PROGRESS);
        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单,我们只想改：status = 已接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        /**
         * 创建一个 Orders 订单对象
         * 接收前端传过来的数据：订单 id
            要改成的订单状态：已接单
            调用 mapper 更新数据库
         */
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }
    /**
     * 拒单
     * @param ordersConfirmDTO
     */
    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        /**
         * 创建一个 Orders 订单对象
         * 接收前端传过来的数据：订单 id
            要改成的订单状态：已拒单
            调用 mapper 更新数据库
         */
        Orders orders = new Orders();
        orders.setId(ordersRejectionDTO.getId());
        // 订单只有存在且状态为2（待接单）才可以拒单
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
     * @param id
     */
    @Override
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.selectById(ordersCancelDTO.getId());
        Integer ordersStatus = orders.getStatus();
        if(ordersStatus != Orders.PENDING_PAYMENT){
            orders.setId(ordersCancelDTO.getId());
            //更新订单状态为已取消
            orders.setStatus(Orders.CANCELLED);
            //更新取消原因
            orders.setCancelReason("管理员取消订单");
            //更新取消时间
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }
    /**
     * 派送订单
     * @param id
     */
    @Override
    public void deliveryOrder(Long id) {
        /**
         * 思路：
         * 根据id查询订单
         * 校验订单是否存在，并且状态为3
         * 更新订单状态,状态转为派送中
         */
        Orders orders = orderMapper.selectById(id);
        Integer ordersStatus = orders.getStatus();
        if(orders != null && ordersStatus.equals(Orders.CONFIRMED)){
            //更新订单状态为派送中
            orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
            orderMapper.update(orders);
        }
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void completeOrder(Long id) {
        /**
         * 思路：
         * 根据id查询订单
         * 校验订单是否存在，并且状态为5
         * 更新订单状态,状态转为已完成
         */
        Orders orders = orderMapper.selectById(id);
        Integer ordersStatus = orders.getStatus();
        if(orders != null && ordersStatus.equals(Orders.DELIVERY_IN_PROGRESS)){
            //更新订单状态为已完成
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }
    
    /**
     * 校验用户收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

	
}