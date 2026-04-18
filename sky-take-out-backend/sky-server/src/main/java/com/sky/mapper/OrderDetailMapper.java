package com.sky.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;


/**
 * 订单详情Mapper
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail>  {
    /**
     * 批量插入订单明细
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);
    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> selectList(Long orderId);

}
