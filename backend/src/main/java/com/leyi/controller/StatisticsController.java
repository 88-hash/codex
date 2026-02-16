package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        return Result.success(statisticsService.getDashboard());
    }

    @GetMapping("/topSales")
    public Result<?> topSales(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getTopSales(days, limit));
    }

    @GetMapping("/salesTrend")
    public Result<?> salesTrend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(statisticsService.getSalesTrend(days));
    }

    @GetMapping("/lowStock")
    public Result<?> lowStock() {
        return Result.success(statisticsService.getLowStockGoods());
    }

    @GetMapping("/expiring")
    public Result<?> expiring(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(statisticsService.getExpiringGoods(days));
    }
}



