package com.sky.service.impl;

import com.sky.dto.SeckillActivityDTO;
import com.sky.dto.SeckillProductDTO;
import com.sky.entity.SeckillActivity;
import com.sky.entity.SeckillProduct;
import com.sky.exception.BaseException;
import com.sky.mapper.SeckillActivityMapper;
import com.sky.mapper.SeckillProductMapper;
import com.sky.service.SeckillService;
import com.sky.vo.SeckillActivityVO;
import com.sky.vo.SeckillProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 秒杀服务实现类
 *
 * 实现思路：
 * 1. 活动管理：创建/编辑/删除秒杀活动，设置活动时间
 * 2. 商品管理：将商品加入秒杀活动，设置秒杀价和库存
 * 3. 库存控制：秒杀库存独立管理，与正常库存分离
 *
 * 核心设计：
 * - 秒杀库存独立于正常库存，互不影响
 * - 使用乐观锁扣减秒杀库存，防止超卖
 * - 活动状态根据时间自动判断
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    /**
     * 创建秒杀活动
     */
    @Override
    public void createActivity(SeckillActivityDTO dto) {
        SeckillActivity activity = new SeckillActivity();
        BeanUtils.copyProperties(dto, activity);

        // 根据时间判断初始状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dto.getStartTime())) {
            activity.setStatus(SeckillActivity.STATUS_NOT_STARTED);
        } else if (now.isAfter(dto.getEndTime())) {
            activity.setStatus(SeckillActivity.STATUS_ENDED);
        } else {
            activity.setStatus(SeckillActivity.STATUS_IN_PROGRESS);
        }

        seckillActivityMapper.insert(activity);
        log.info("创建秒杀活动成功：id={}, name={}", activity.getId(), activity.getName());
    }

    /**
     * 更新秒杀活动
     */
    @Override
    public void updateActivity(SeckillActivityDTO dto) {
        SeckillActivity activity = seckillActivityMapper.selectById(dto.getId());
        if (activity == null) {
            throw new BaseException("活动不存在");
        }

        BeanUtils.copyProperties(dto, activity);

        // 重新计算状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dto.getStartTime())) {
            activity.setStatus(SeckillActivity.STATUS_NOT_STARTED);
        } else if (now.isAfter(dto.getEndTime())) {
            activity.setStatus(SeckillActivity.STATUS_ENDED);
        } else {
            activity.setStatus(SeckillActivity.STATUS_IN_PROGRESS);
        }

        seckillActivityMapper.updateById(activity);
        log.info("更新秒杀活动成功：id={}", dto.getId());
    }

    /**
     * 删除秒杀活动
     */
    @Override
    public void deleteActivity(Long id) {
        seckillActivityMapper.deleteById(id);
        log.info("删除秒杀活动：id={}", id);
    }

    /**
     * 查询进行中的秒杀活动
     *
     * 为什么：用户端展示当前可参与的秒杀活动
     * 怎么做的：查询状态为进行中且时间有效的活动
     */
    @Override
    public List<SeckillActivityVO> getInProgressActivities() {
        List<SeckillActivity> activities = seckillActivityMapper.selectInProgress();

        List<SeckillActivityVO> voList = new ArrayList<>();
        for (SeckillActivity activity : activities) {
            SeckillActivityVO vo = new SeckillActivityVO();
            BeanUtils.copyProperties(activity, vo);

            // 查询活动下的商品
            List<SeckillProductVO> products = seckillProductMapper.selectByActivityId(activity.getId());
            vo.setProductList(products);

            voList.add(vo);
        }
        return voList;
    }

    /**
     * 添加秒杀商品
     *
     * 为什么：将商品加入秒杀活动
     * 怎么做的：
     * 1. 校验活动是否存在
     * 2. 校验商品是否已加入该活动
     * 3. 插入秒杀商品记录
     */
    @Override
    public void addSeckillProduct(SeckillProductDTO dto) {
        // 校验活动
        SeckillActivity activity = seckillActivityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new BaseException("秒杀活动不存在");
        }

        // 校验是否已存在
        SeckillProduct exist = seckillProductMapper.selectByActivityAndProduct(dto.getActivityId(), dto.getProductId());
        if (exist != null) {
            throw new BaseException("该商品已加入此活动");
        }

        SeckillProduct product = new SeckillProduct();
        BeanUtils.copyProperties(dto, product);
        product.setSoldCount(0);
        product.setStatus(SeckillProduct.STATUS_ENABLED);

        seckillProductMapper.insert(product);
        log.info("添加秒杀商品成功：activityId={}, productId={}", dto.getActivityId(), dto.getProductId());
    }

    /**
     * 更新秒杀商品
     */
    @Override
    public void updateSeckillProduct(SeckillProductDTO dto) {
        SeckillProduct product = new SeckillProduct();
        BeanUtils.copyProperties(dto, product);
        seckillProductMapper.updateById(product);
        log.info("更新秒杀商品成功：id={}", dto.getId());
    }

    /**
     * 删除秒杀商品
     */
    @Override
    public void deleteSeckillProduct(Long id) {
        seckillProductMapper.deleteById(id);
        log.info("删除秒杀商品：id={}", id);
    }

    /**
     * 根据活动ID查询秒杀商品列表
     */
    @Override
    public List<SeckillProductVO> getProductsByActivityId(Long activityId) {
        return seckillProductMapper.selectByActivityId(activityId);
    }
}
