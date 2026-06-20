package com.sky.controller.admin;

import com.sky.annotation.RequireRole;
import com.sky.dto.CouponTemplateDTO;
import com.sky.dto.CouponTemplatePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CouponService;
import com.sky.vo.CouponTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端-优惠券管理控制器
 *
 * 为什么创建这个类：
 * - 提供后台管理优惠券模板的RESTful API
 * - 支持优惠券的增删改查和状态管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/coupon")
@Api(tags = "管理端-优惠券管理")
@RequireRole({"admin", "manager"})
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 创建优惠券模板
     */
    @PostMapping("/template")
    @ApiOperation("创建优惠券模板")
    public Result<String> createTemplate(@Valid @RequestBody CouponTemplateDTO dto) {
        log.info("创建优惠券模板：{}", dto);
        couponService.createTemplate(dto);
        return Result.success();
    }

    /**
     * 分页查询优惠券模板
     */
    @GetMapping("/template/page")
    @ApiOperation("分页查询优惠券模板")
    public Result<PageResult> pageQuery(CouponTemplatePageQueryDTO queryDTO) {
        log.info("分页查询优惠券模板：{}", queryDTO);
        PageResult pageResult = couponService.pageQueryTemplate(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询优惠券模板
     */
    @GetMapping("/template/{id}")
    @ApiOperation("根据ID查询优惠券模板")
    public Result<CouponTemplateVO> getById(@PathVariable Long id) {
        log.info("查询优惠券模板：id={}", id);
        CouponTemplateVO vo = couponService.getTemplateById(id);
        return Result.success(vo);
    }

    /**
     * 更新优惠券模板
     */
    @PutMapping("/template")
    @ApiOperation("更新优惠券模板")
    public Result<String> updateTemplate(@Valid @RequestBody CouponTemplateDTO dto) {
        log.info("更新优惠券模板：{}", dto);
        couponService.updateTemplate(dto);
        return Result.success();
    }

    /**
     * 启用/停用优惠券模板
     */
    @PostMapping("/template/status/{id}")
    @ApiOperation("启用/停用优惠券模板")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新优惠券模板状态：id={}, status={}", id, status);
        couponService.updateTemplateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除优惠券模板
     */
    @DeleteMapping("/template/{id}")
    @ApiOperation("删除优惠券模板")
    public Result<String> deleteTemplate(@PathVariable Long id) {
        log.info("删除优惠券模板：id={}", id);
        couponService.deleteTemplate(id);
        return Result.success();
    }
}
