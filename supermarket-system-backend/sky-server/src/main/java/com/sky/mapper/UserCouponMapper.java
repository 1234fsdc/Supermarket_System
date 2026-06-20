package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户优惠券Mapper
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 查询用户已领取的某优惠券数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 查询用户可用的优惠券列表
     */
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} AND status = 1 AND expire_time > NOW() ORDER BY expire_time ASC")
    List<UserCoupon> selectUsableByUserId(Long userId);

    /**
     * 查询用户的所有优惠券
     */
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserCoupon> selectByUserId(Long userId);
}
