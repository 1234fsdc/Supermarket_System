package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SeckillActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 秒杀活动Mapper
 */
@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {

    /**
     * 查询进行中的活动
     */
    @Select("SELECT * FROM seckill_activity WHERE status = 1 AND start_time <= NOW() AND end_time >= NOW()")
    List<SeckillActivity> selectInProgress();
}
