package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.CouponService;
import com.sky.vo.CouponDiscountVO;
import com.sky.vo.UserCouponVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端-优惠券控制器
 *
 * 为什么创建这个类：
 * - 提供用户领取优惠券、查询优惠券的接口
 * - 支持优惠券使用前的优惠金额计算
 */
@Slf4j
@RestController
@RequestMapping("/user/coupon")
@Api(tags = "用户端-优惠券相关接口")
public class UserCouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 领取优惠券
     */
    @PostMapping("/receive/{couponId}")
    @ApiOperation("领取优惠券")
    public Result<String> receiveCoupon(@PathVariable Long couponId) {
        log.info("领取优惠券：couponId={}", couponId);
        couponService.receiveCoupon(couponId);
        return Result.success();
    }

    /**
     * 查询用户优惠券列表
     */
    @GetMapping("/list")
    @ApiOperation("查询用户优惠券列表")
    public Result<List<UserCouponVO>> getUserCoupons(@RequestParam(required = false) Integer status) {
        log.info("查询用户优惠券列表：status={}", status);
        List<UserCouponVO> list = couponService.getUserCoupons(status);
        return Result.success(list);
    }

    /**
     * 查询用户可用优惠券（下单时）
     */
    @GetMapping("/usable")
    @ApiOperation("查询用户可用优惠券")
    public Result<List<UserCouponVO>> getUsableCoupons() {
        log.info("查询用户可用优惠券");
        List<UserCouponVO> list = couponService.getUsableCoupons();
        return Result.success(list);
    }

    /**
     * 计算优惠券优惠金额
     */
    @GetMapping("/calculate")
    @ApiOperation("计算优惠券优惠金额")
    public Result<CouponDiscountVO> calculateDiscount(
            @RequestParam Long userCouponId,
            @RequestParam BigDecimal orderAmount) {
        log.info("计算优惠券优惠：userCouponId={}, orderAmount={}", userCouponId, orderAmount);
        CouponDiscountVO vo = couponService.calculateDiscount(userCouponId, orderAmount);
        return Result.success(vo);
    }
}
