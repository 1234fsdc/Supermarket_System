package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.SeckillOrderService;
import com.sky.service.SeckillService;
import com.sky.vo.SeckillActivityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-秒杀活动控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/seckill")
@Api(tags = "用户端-秒杀活动相关接口")
public class UserSeckillController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 查询进行中的秒杀活动
     */
    @GetMapping("/activities")
    @ApiOperation("查询进行中的秒杀活动")
    public Result<List<SeckillActivityVO>> getInProgressActivities() {
        log.info("查询进行中的秒杀活动");
        List<SeckillActivityVO> list = seckillService.getInProgressActivities();
        return Result.success(list);
    }

    /**
     * 秒杀抢购
     */
    @PostMapping("/buy/{productId}")
    @ApiOperation("秒杀抢购")
    public Result<String> seckill(@PathVariable Long productId) {
        log.info("用户发起秒杀请求，商品ID：{}", productId);
        return seckillOrderService.seckill(productId);
    }

    /**
     * 查询秒杀结果
     */
    @GetMapping("/result/{productId}")
    @ApiOperation("查询秒杀结果")
    public Result<String> getSeckillResult(@PathVariable Long productId) {
        log.info("查询秒杀结果，商品ID：{}", productId);
        return seckillOrderService.getSeckillResult(productId);
    }
}
