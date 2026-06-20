package com.sky.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.context.BaseContext;
import com.sky.dto.CouponTemplateDTO;
import com.sky.dto.CouponTemplatePageQueryDTO;
import com.sky.entity.CouponTemplate;
import com.sky.entity.UserCoupon;
import com.sky.exception.BaseException;
import com.sky.mapper.CouponTemplateMapper;
import com.sky.mapper.UserCouponMapper;
import com.sky.result.PageResult;
import com.sky.service.CouponService;
import com.sky.vo.CouponDiscountVO;
import com.sky.vo.CouponTemplateVO;
import com.sky.vo.UserCouponVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券服务实现类
 *
 * 为什么创建这个类：
 * - 实现优惠券相关的业务逻辑
 * - 处理优惠券模板的CRUD操作
 * - 处理用户领券、用券等业务
 *
 * 怎么做的：
 * - 实现CouponService接口
 * - 使用@Autowired注入Mapper
 * - 使用@Transactional保证事务一致性
 */
@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    /**
     * 优惠券模板Mapper
     * 为什么：操作优惠券模板表
     */
    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    /**
     * 用户优惠券Mapper
     * 为什么：操作用户优惠券表
     */
    @Autowired
    private UserCouponMapper userCouponMapper;

    /**
     * 创建优惠券模板
     *
     * @param couponTemplateDTO 优惠券模板信息
     */
    @Override
    @Transactional
    public void createTemplate(CouponTemplateDTO couponTemplateDTO) {
        log.info("创建优惠券模板：{}", couponTemplateDTO.getName());

        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(couponTemplateDTO, template);

        // 设置初始剩余数量等于总数量
        template.setRemainCount(couponTemplateDTO.getTotalCount());
        template.setStatus(CouponTemplate.STATUS_ENABLED);

        couponTemplateMapper.insert(template);
    }

    /**
     * 分页查询优惠券模板
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryTemplate(CouponTemplatePageQueryDTO queryDTO) {
        log.info("优惠券模板分页查询，页码：{}，每页条数：{}", queryDTO.getPage(), queryDTO.getPageSize());

        Page<CouponTemplateVO> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<CouponTemplateVO> pageResult = couponTemplateMapper.pageQuery(page, queryDTO);

        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
    }

    /**
     * 根据ID查询优惠券模板
     *
     * @param id 优惠券模板ID
     * @return 优惠券模板详情
     */
    @Override
    public CouponTemplateVO getTemplateById(Long id) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new BaseException("优惠券模板不存在");
        }
        CouponTemplateVO vo = new CouponTemplateVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }

    /**
     * 更新优惠券模板
     *
     * @param couponTemplateDTO 优惠券模板信息
     */
    @Override
    @Transactional
    public void updateTemplate(CouponTemplateDTO couponTemplateDTO) {
        log.info("更新优惠券模板：{}", couponTemplateDTO.getId());

        CouponTemplate template = couponTemplateMapper.selectById(couponTemplateDTO.getId());
        if (template == null) {
            throw new BaseException("优惠券模板不存在");
        }

        // 如果已经有人领取，不允许修改关键字段
        int receivedCount = template.getTotalCount() - template.getRemainCount();
        if (receivedCount > 0) {
            // 只允许修改名称、时间、状态等非关键字段
            CouponTemplate update = new CouponTemplate();
            update.setId(couponTemplateDTO.getId());
            update.setName(couponTemplateDTO.getName());
            update.setStartTime(couponTemplateDTO.getStartTime());
            update.setEndTime(couponTemplateDTO.getEndTime());
            update.setStatus(couponTemplateDTO.getStatus());
            couponTemplateMapper.updateById(update);
        } else {
            CouponTemplate update = new CouponTemplate();
            BeanUtils.copyProperties(couponTemplateDTO, update);
            update.setRemainCount(couponTemplateDTO.getTotalCount());
            couponTemplateMapper.updateById(update);
        }
    }

    /**
     * 启用/停用优惠券模板
     *
     * @param id 优惠券模板ID
     * @param status 状态
     */
    @Override
    public void updateTemplateStatus(Long id, Integer status) {
        CouponTemplate template = new CouponTemplate();
        template.setId(id);
        template.setStatus(status);
        couponTemplateMapper.updateById(template);
        log.info("更新优惠券模板状态：id={}, status={}", id, status);
    }

    /**
     * 删除优惠券模板
     *
     * @param id 优惠券模板ID
     */
    @Override
    public void deleteTemplate(Long id) {
        couponTemplateMapper.deleteById(id);
        log.info("删除优惠券模板：id={}", id);
    }

    /**
     * 用户领取优惠券
     *
     * @param couponId 优惠券模板ID
     */
    @Override
    @Transactional
    public void receiveCoupon(Long couponId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户领取优惠券：userId={}, couponId={}", userId, couponId);

        // 1. 查询优惠券模板
        CouponTemplate template = couponTemplateMapper.selectById(couponId);
        if (template == null) {
            throw new BaseException("优惠券不存在");
        }
        if (template.getStatus() != CouponTemplate.STATUS_ENABLED) {
            throw new BaseException("优惠券已停用");
        }

        // 2. 校验时间
        LocalDateTime now = LocalDateTime.now();
        if (template.getStartTime() != null && now.isBefore(template.getStartTime())) {
            throw new BaseException("优惠券活动未开始");
        }
        if (template.getEndTime() != null && now.isAfter(template.getEndTime())) {
            throw new BaseException("优惠券活动已结束");
        }

        // 3. 校验用户领取限制
        int userReceivedCount = userCouponMapper.countByUserAndCoupon(userId, couponId);
        if (userReceivedCount >= template.getLimitPerUser()) {
            throw new BaseException("您已达到该优惠券的领取上限");
        }

        // 4. 扣减模板库存（乐观锁）
        int affected = couponTemplateMapper.deductRemainCount(couponId);
        if (affected == 0) {
            throw new BaseException("优惠券已被领完");
        }

        // 5. 计算过期时间
        LocalDateTime expireTime;
        if (template.getValidDays() != null) {
            expireTime = now.plusDays(template.getValidDays());
        } else {
            expireTime = template.getEndTime();
        }

        // 6. 创建用户优惠券记录（快照模式）
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .couponName(template.getName())
                .couponType(template.getType())
                .discountValue(template.getDiscountValue())
                .minSpend(template.getMinSpend())
                .status(UserCoupon.STATUS_UNUSED)
                .receiveTime(now)
                .expireTime(expireTime)
                .build();

        userCouponMapper.insert(userCoupon);
    }

    /**
     * 查询用户优惠券列表
     *
     * @param status 优惠券状态
     * @return 用户优惠券列表
     */
    @Override
    public List<UserCouponVO> getUserCoupons(Integer status) {
        Long userId = BaseContext.getCurrentId();
        List<UserCoupon> list = userCouponMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(status != null, UserCoupon::getStatus, status)
                        .orderByDesc(UserCoupon::getCreateTime)
        );

        List<UserCouponVO> voList = new ArrayList<>();
        for (UserCoupon uc : list) {
            UserCouponVO vo = new UserCouponVO();
            BeanUtils.copyProperties(uc, vo);
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 查询用户可用优惠券
     *
     * @return 可用优惠券列表
     */
    @Override
    public List<UserCouponVO> getUsableCoupons() {
        Long userId = BaseContext.getCurrentId();
        List<UserCoupon> list = userCouponMapper.selectUsableByUserId(userId);

        List<UserCouponVO> voList = new ArrayList<>();
        for (UserCoupon uc : list) {
            UserCouponVO vo = new UserCouponVO();
            BeanUtils.copyProperties(uc, vo);
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 计算优惠券优惠金额
     *
     * @param userCouponId 用户优惠券ID
     * @param orderAmount 订单金额
     * @return 优惠计算结果
     */
    @Override
    public CouponDiscountVO calculateDiscount(Long userCouponId, BigDecimal orderAmount) {
        // 1. 查询用户优惠券
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            return CouponDiscountVO.builder()
                    .usable(false)
                    .reason("优惠券不存在")
                    .originalAmount(orderAmount)
                    .finalAmount(orderAmount)
                    .build();
        }

        // 2. 校验状态
        if (userCoupon.getStatus() != UserCoupon.STATUS_UNUSED) {
            return CouponDiscountVO.builder()
                    .usable(false)
                    .reason("优惠券已使用或已过期")
                    .originalAmount(orderAmount)
                    .finalAmount(orderAmount)
                    .build();
        }

        // 3. 校验有效期
        if (LocalDateTime.now().isAfter(userCoupon.getExpireTime())) {
            return CouponDiscountVO.builder()
                    .usable(false)
                    .reason("优惠券已过期")
                    .originalAmount(orderAmount)
                    .finalAmount(orderAmount)
                    .build();
        }

        // 4. 校验最低消费
        if (orderAmount.compareTo(userCoupon.getMinSpend()) < 0) {
            return CouponDiscountVO.builder()
                    .usable(false)
                    .reason("订单金额未满" + userCoupon.getMinSpend() + "元")
                    .originalAmount(orderAmount)
                    .finalAmount(orderAmount)
                    .build();
        }

        // 5. 计算优惠金额
        BigDecimal discountAmount;
        if (userCoupon.getCouponType() == CouponTemplate.TYPE_DISCOUNT) {
            // 折扣券：按比例计算
            discountAmount = orderAmount.multiply(BigDecimal.ONE.subtract(userCoupon.getDiscountValue()));
        } else {
            // 满减券/新人券：固定金额
            discountAmount = userCoupon.getDiscountValue();
        }

        // 优惠金额不能超过订单金额
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }

        // 保留两位小数
        discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        return CouponDiscountVO.builder()
                .userCouponId(userCouponId)
                .couponName(userCoupon.getCouponName())
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .usable(true)
                .reason("可以使用")
                .build();
    }

    /**
     * 使用优惠券
     *
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     */
    @Override
    @Transactional
    public void useCoupon(Long userCouponId, Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new BaseException("优惠券不存在");
        }
        if (userCoupon.getStatus() != UserCoupon.STATUS_UNUSED) {
            throw new BaseException("优惠券状态异常");
        }

        // 更新优惠券状态
        UserCoupon update = new UserCoupon();
        update.setId(userCouponId);
        update.setStatus(UserCoupon.STATUS_USED);
        update.setUseTime(LocalDateTime.now());
        update.setOrderId(orderId);
        userCouponMapper.updateById(update);

        log.info("使用优惠券成功：userCouponId={}, orderId={}", userCouponId, orderId);
    }
}
