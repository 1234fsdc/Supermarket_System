package com.sky.controller.admin;

import com.sky.annotation.RequireRole;
import com.sky.dto.SeckillActivityDTO;
import com.sky.dto.SeckillProductDTO;
import com.sky.result.Result;
import com.sky.service.SeckillService;
import com.sky.vo.SeckillActivityVO;
import com.sky.vo.SeckillProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理端-秒杀活动管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/seckill")
@Api(tags = "管理端-秒杀活动管理")
@RequireRole({"admin", "manager"})
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @PostMapping("/activity")
    @ApiOperation("创建秒杀活动")
    public Result<String> createActivity(@Valid @RequestBody SeckillActivityDTO dto) {
        log.info("创建秒杀活动：{}", dto);
        seckillService.createActivity(dto);
        return Result.success();
    }

    @PutMapping("/activity")
    @ApiOperation("更新秒杀活动")
    public Result<String> updateActivity(@Valid @RequestBody SeckillActivityDTO dto) {
        log.info("更新秒杀活动：{}", dto);
        seckillService.updateActivity(dto);
        return Result.success();
    }

    @DeleteMapping("/activity/{id}")
    @ApiOperation("删除秒杀活动")
    public Result<String> deleteActivity(@PathVariable Long id) {
        log.info("删除秒杀活动：id={}", id);
        seckillService.deleteActivity(id);
        return Result.success();
    }

    @GetMapping("/activity/inProgress")
    @ApiOperation("查询进行中的秒杀活动")
    public Result<List<SeckillActivityVO>> getInProgressActivities() {
        log.info("查询进行中的秒杀活动");
        List<SeckillActivityVO> list = seckillService.getInProgressActivities();
        return Result.success(list);
    }

    @PostMapping("/product")
    @ApiOperation("添加秒杀商品")
    public Result<String> addSeckillProduct(@Valid @RequestBody SeckillProductDTO dto) {
        log.info("添加秒杀商品：{}", dto);
        seckillService.addSeckillProduct(dto);
        return Result.success();
    }

    @PutMapping("/product")
    @ApiOperation("更新秒杀商品")
    public Result<String> updateSeckillProduct(@Valid @RequestBody SeckillProductDTO dto) {
        log.info("更新秒杀商品：{}", dto);
        seckillService.updateSeckillProduct(dto);
        return Result.success();
    }

    @DeleteMapping("/product/{id}")
    @ApiOperation("删除秒杀商品")
    public Result<String> deleteSeckillProduct(@PathVariable Long id) {
        log.info("删除秒杀商品：id={}", id);
        seckillService.deleteSeckillProduct(id);
        return Result.success();
    }

    @GetMapping("/product/{activityId}")
    @ApiOperation("根据活动ID查询秒杀商品")
    public Result<List<SeckillProductVO>> getProductsByActivityId(@PathVariable Long activityId) {
        log.info("查询秒杀商品：activityId={}", activityId);
        List<SeckillProductVO> list = seckillService.getProductsByActivityId(activityId);
        return Result.success(list);
    }
}
