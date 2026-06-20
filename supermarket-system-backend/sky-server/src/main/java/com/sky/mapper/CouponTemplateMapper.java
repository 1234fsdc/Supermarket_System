package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.CouponTemplatePageQueryDTO;
import com.sky.entity.CouponTemplate;
import com.sky.vo.CouponTemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 优惠券模板Mapper
 */
@Mapper
public interface CouponTemplateMapper extends BaseMapper<CouponTemplate> {

    /**
     * 分页查询优惠券模板
     */
    IPage<CouponTemplateVO> pageQuery(Page<CouponTemplateVO> page, @Param("query") CouponTemplatePageQueryDTO queryDTO);

    /**
     * 扣减库存（发放优惠券时）
     */
    @Update("UPDATE coupon_template SET remain_count = remain_count - 1 WHERE id = #{id} AND remain_count > 0")
    int deductRemainCount(@Param("id") Long id);
}
