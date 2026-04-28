package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.OrderSubmitDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderSubmitVO;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    int insert(OrderSubmitDTO orderSubmitDTO);
    /**
     * 向订单表插入一条数据
     * @param orders
     * @return 影响的行数

     */
    int insert(Orders orders);

    /**
     * 更新订单信息
     * @param orders
     */
    void update(Orders orders);
    /**
     * 历史订单分页查询
     * @param pageResult
     * @param ordersPageQueryDTO
     * @return
     */
    IPage<Orders> pageQuery(Page<Orders> pageResult, OrdersPageQueryDTO ordersPageQueryDTO);
    /**
     * 根据状态查询订单数量
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer getStatistics(Integer status);

}
