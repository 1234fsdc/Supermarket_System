package com.sky.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sky.annotation.RequireRole;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.DailyTrendVO;
import com.sky.vo.DashboardOverviewVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.StatisticsReportVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 工作台与数据统计控制器
 *
 * 为什么创建这个类：
 * - 集中管理所有统计数据相关的 API，避免把统计逻辑散落在各个控制器中
 * - 提供高效的数据库聚合查询接口，替代前端自行计算的方式
 *
 * 实现思路：
 * - 所有统计数据通过 SQL 聚合函数（SUM/COUNT）在数据库层完成
 * - 环比趋势在后端统一计算，前端只负责展示
 * - 与 OrderController 分离，职责更单一
 */
@RestController
@RequestMapping({"/dashboard", "/api/dashboard"})
@Api(tags = "数据统计接口")
@Slf4j
@RequireRole({"admin", "manager"})
public class DashboardController {

    @Autowired
    private OrderService orderService;

    /**
     * 工作台概览
     *
     * 为什么：管理端首页需要展示今日营业概况
     * 怎么做的：调用 Service 聚合查询今日营业额、订单数、商品数、员工数、订单状态统计
     *
     * @return 概览数据
     */
    @GetMapping("/overview")
    @ApiOperation("工作台概览")
    public Result<DashboardOverviewVO> overview() {
        log.info("查询工作台概览数据");
        DashboardOverviewVO overview = orderService.getDashboardOverview();
        return Result.success(overview);
    }

    /**
     * 数据统计报表
     *
     * 为什么：统计页需要展示完整的统计报表
     * 怎么做的：查询今日/昨日数据，计算完成率、客单价及环比趋势
     *
     * @return 统计报表
     */
    @GetMapping("/report")
    @ApiOperation("数据统计报表")
    public Result<StatisticsReportVO> report() {
        log.info("查询数据统计报表");
        StatisticsReportVO report = orderService.getStatisticsReport();
        return Result.success(report);
    }

    /**
     * 每日营业额趋势
     *
     * 为什么：折线图需要展示每日营业额变化
     *
     * @param days 查询天数（默认7天）
     * @return 每日营业额列表
     */
    @GetMapping("/turnoverTrend")
    @ApiOperation("每日营业额趋势")
    public Result<List<DailyTrendVO>> turnoverTrend(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        log.info("查询近{}天营业额趋势", days);
        List<DailyTrendVO> trendList = orderService.getDailyTurnoverTrend(days);
        return Result.success(trendList);
    }

    /**
     * 每日订单数量趋势
     *
     * 为什么：柱状图需要展示每日订单数量变化
     *
     * @param days 查询天数（默认7天）
     * @return 每日订单数列表
     */
    @GetMapping("/orderCountTrend")
    @ApiOperation("每日订单数量趋势")
    public Result<List<DailyTrendVO>> orderCountTrend(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        log.info("查询近{}天订单数量趋势", days);
        List<DailyTrendVO> trendList = orderService.getDailyOrderCountTrend(days);
        return Result.success(trendList);
    }

    /**
     * 订单状态统计
     *
     * 为什么：饼图需要展示各状态订单分布
     * 复用已有的 statistics 接口，但这里提供一个独立的 Dashboard 端点
     *
     * @return 订单状态统计
     */
    @GetMapping("/orderStatistics")
    @ApiOperation("订单状态统计")
    public Result<OrderStatisticsVO> orderStatistics() {
        log.info("查询订单状态统计");
        OrderStatisticsVO statistics = orderService.statistics();
        return Result.success(statistics);
    }
}
