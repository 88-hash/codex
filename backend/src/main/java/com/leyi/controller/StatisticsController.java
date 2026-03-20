package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.security.AuthGuard;
import com.leyi.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/dashboard")
    public Result<?> dashboard(HttpServletRequest request) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getDashboard());
    }

    @GetMapping("/topSales")
    public Result<?> topSales(
            HttpServletRequest request,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "10") Integer limit) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getTopSales(days, limit));
    }

    @GetMapping("/salesTrend")
    public Result<?> salesTrend(HttpServletRequest request, @RequestParam(defaultValue = "7") Integer days) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getSalesTrend(days));
    }

    @GetMapping("/lowStock")
    public Result<?> lowStock(HttpServletRequest request) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getLowStockGoods());
    }

    @GetMapping("/expiring")
    public Result<?> expiring(HttpServletRequest request, @RequestParam(defaultValue = "30") Integer days) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getExpiringGoods(days));
    }

    @GetMapping("/orderStatus")
    public Result<?> orderStatus(HttpServletRequest request) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getOrderStatusSummary());
    }

    @GetMapping("/categorySales")
    public Result<?> categorySales(HttpServletRequest request, @RequestParam(defaultValue = "30") Integer days) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getCategorySales(days));
    }

    @GetMapping("/hourlyOrders")
    public Result<?> hourlyOrders(HttpServletRequest request) {
        authGuard.requireAdmin(request);
        return Result.success(statisticsService.getHourlyOrders());
    }
}
