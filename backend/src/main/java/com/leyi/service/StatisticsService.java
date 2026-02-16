package com.leyi.service;

import com.leyi.entity.Goods;
import com.leyi.mapper.GoodsMapper;
import com.leyi.mapper.OrderItemMapper;
import com.leyi.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        
        // 今日营业额
        BigDecimal todaySales = orderMapper.getTodaySales();
        result.put("todaySales", todaySales);
        
        // 今日订单数
        int todayOrders = orderMapper.countTodayOrders();
        result.put("todayOrders", todayOrders);
        
        // 今日核销数
        int todayVerified = orderMapper.countTodayVerified();
        result.put("todayVerified", todayVerified);
        
        // 核销转化率
        double verifyRate = todayOrders > 0 ? (double) todayVerified / todayOrders * 100 : 0;
        result.put("verifyRate", Math.round(verifyRate * 100) / 100.0);
        
        // 库存预警商品数
        List<Goods> lowStockGoods = goodsMapper.findLowStock();
        result.put("lowStockCount", lowStockGoods.size());
        
        // 临期商品数(30天内)
        List<Goods> expiringGoods = goodsMapper.findExpiringSoon(30);
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
}



