package com.sky.service;

import com.sky.dto.CouponTemplateDTO;
import com.sky.dto.CouponTemplatePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.CouponDiscountVO;
import com.sky.vo.CouponTemplateVO;
import com.sky.vo.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 *
 * 为什么创建这个接口：
 * - 定义优惠券相关的业务操作
 * - 包括优惠券模板管理、用户领券、用券等
 */
public interface CouponService {

    /**
     * 创建优惠券模板
     */
    void createTemplate(CouponTemplateDTO couponTemplateDTO);

    /**
     * 分页查询优惠券模板
     */
    PageResult pageQueryTemplate(CouponTemplatePageQueryDTO queryDTO);

    /**
     * 根据ID查询优惠券模板
     */
    CouponTemplateVO getTemplateById(Long id);

    /**
     * 更新优惠券模板
     */
    void updateTemplate(CouponTemplateDTO couponTemplateDTO);

    /**
     * 启用/停用优惠券模板
     */
    void updateTemplateStatus(Long id, Integer status);

    /**
     * 删除优惠券模板
     */
    void deleteTemplate(Long id);

    /**
     * 用户领取优惠券
     */
    void receiveCoupon(Long couponId);

    /**
     * 查询用户优惠券列表
     */
    List<UserCouponVO> getUserCoupons(Integer status);

    /**
     * 查询用户可用优惠券（下单时）
     */
    List<UserCouponVO> getUsableCoupons();

    /**
     * 计算优惠券优惠金额
     */
    CouponDiscountVO calculateDiscount(Long userCouponId, BigDecimal orderAmount);

    /**
     * 使用优惠券（订单提交时调用）
     */
    void useCoupon(Long userCouponId, Long orderId);
}
