package com.leyi.service;

import com.leyi.entity.Goods;
import com.leyi.mapper.GoodsMapper;
import com.leyi.mapper.OrderItemMapper;
import com.leyi.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();

        BigDecimal todaySales = safeAmount(orderMapper.getTodaySales());
        BigDecimal yesterdaySales = safeAmount(orderMapper.getYesterdaySales());
        int todayOrders = orderMapper.countTodayOrders();
        int todayVerified = orderMapper.countTodayVerified();
        int pendingOrders = orderMapper.countPendingOrders();
        List<Goods> lowStockGoods = goodsMapper.findLowStock();
        List<Goods> expiringGoods = goodsMapper.findExpiringSoon(30);

        result.put("todaySales", todaySales);
        result.put("todayOrders", todayOrders);
        result.put("todayVerified", todayVerified);
        result.put("pendingOrders", pendingOrders);
        result.put("verifyRate", roundPercentage(todayOrders == 0 ? 0D : (double) todayVerified / todayOrders * 100));
        result.put("salesGrowth", calculateSalesGrowth(todaySales, yesterdaySales));
        result.put("lowStockCount", lowStockGoods.size());
        result.put("expiringCount", expiringGoods.size());
        return result;
    }

    public List<Map<String, Object>> getTopSales(Integer days, Integer limit) {
        return orderItemMapper.getTopSales(days, limit);
    }

    public List<Map<String, Object>> getSalesTrend(Integer days) {
        return orderMapper.getSalesTrend(days);
    }

    public List<Goods> getLowStockGoods() {
        return goodsMapper.findLowStock();
    }

    public List<Goods> getExpiringGoods(Integer days) {
        return goodsMapper.findExpiringSoon(days);
    }

    public List<Map<String, Object>> getOrderStatusSummary() {
        return orderMapper.getOrderStatusSummary();
    }

    public List<Map<String, Object>> getCategorySales(Integer days) {
        return orderItemMapper.getCategorySales(days);
    }

    public List<Map<String, Object>> getHourlyOrders() {
        return orderMapper.getHourlyOrders();
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private double calculateSalesGrowth(BigDecimal todaySales, BigDecimal yesterdaySales) {
        if (yesterdaySales.compareTo(BigDecimal.ZERO) == 0) {
            return todaySales.compareTo(BigDecimal.ZERO) > 0 ? 100.0D : 0.0D;
        }
        BigDecimal growth = todaySales.subtract(yesterdaySales)
                .multiply(BigDecimal.valueOf(100))
                .divide(yesterdaySales, 4, RoundingMode.HALF_UP);
        return roundPercentage(growth.doubleValue());
    }

    private double roundPercentage(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
