package com.sky.service;

import com.sky.dto.SeckillActivityDTO;
import com.sky.dto.SeckillProductDTO;
import com.sky.result.PageResult;
import com.sky.vo.SeckillActivityVO;
import com.sky.vo.SeckillProductVO;

import java.util.List;

/**
 * 秒杀服务接口
 */
public interface SeckillService {

    /**
     * 创建秒杀活动
     */
    void createActivity(SeckillActivityDTO activityDTO);

    /**
     * 更新秒杀活动
     */
    void updateActivity(SeckillActivityDTO activityDTO);

    /**
     * 删除秒杀活动
     */
    void deleteActivity(Long id);

    /**
     * 查询进行中的秒杀活动
     */
    List<SeckillActivityVO> getInProgressActivities();

    /**
     * 添加秒杀商品
     */
    void addSeckillProduct(SeckillProductDTO productDTO);

    /**
     * 更新秒杀商品
     */
    void updateSeckillProduct(SeckillProductDTO productDTO);

    /**
     * 删除秒杀商品
     */
    void deleteSeckillProduct(Long id);

    /**
     * 根据活动ID查询秒杀商品列表
     */
    List<SeckillProductVO> getProductsByActivityId(Long activityId);
}
