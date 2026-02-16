package com.leyi.service;

import com.leyi.common.PageResult;
import com.leyi.entity.*;
import com.leyi.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private VerifyLogMapper verifyLogMapper;

    @Autowired
    private UserMapper userMapper;

    public List<Order> getByUserId(Long userId, String status) {
        List<Order> orders = orderMapper.findByUserId(userId, status);
        for (Order order : orders) {
            order.setItems(orderItemMapper.findByOrderId(order.getId()));
        }
        return orders;
    }

    public PageResult<Order> getList(String status, String keyword, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Order> list = orderMapper.findList(status, keyword, offset, pageSize);
        for (Order order : list) {
            order.setItems(orderItemMapper.findByOrderId(order.getId()));
        }
        Long total = orderMapper.countList(status, keyword);
        return PageResult.of(list, total, pageNum, pageSize);
    }

    public Order getById(Long id) {
        Order order = orderMapper.findById(id);
        if (order != null) {
            order.setItems(orderItemMapper.findByOrderId(id));
        }
        return order;
    }

    public Order getByVerifyCode(String verifyCode) {
        Order order = orderMapper.findByVerifyCode(verifyCode);
        if (order != null) {
            order.setItems(orderItemMapper.findByOrderId(order.getId()));
        }
        return order;
    }

    @Transactional
    public Order create(Long userId, String remark) {
        User user = userMapper.findById(userId);
        List<Cart> cartItems = cartMapper.findCheckedByUserId(userId);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("请选择要结算的商品");
        }

        // 计算总价并校验库存
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            Goods goods = goodsMapper.findById(cart.getGoodsId());
            if (goods == null || goods.getIsOnSale() != 1) {
                throw new RuntimeException("商品 " + cart.getGoodsName() + " 已下架");
            }
            if (goods.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品 " + goods.getName() + " 库存不足");
            }
            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        // 生成订单号
        String orderNo = generateOrderNo();
        // 生成取货码
        String verifyCode = generateVerifyCode();

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setUserPhone(user.getPhone());
        order.setOrderNo(orderNo);
        order.setTotalPrice(totalPrice);
        order.setStatus("pending");
        order.setVerifyCode(verifyCode);
        order.setRemark(remark != null ? remark : "");
        orderMapper.insert(order);

        // 创建订单明细并扣减库存
        for (Cart cart : cartItems) {
            Goods goods = goodsMapper.findById(cart.getGoodsId());
            
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setGoodsId(goods.getId());
            item.setGoodsName(goods.getName());
            item.setGoodsImage(goods.getImageUrl());
            item.setPrice(goods.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(goods.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            orderItemMapper.insert(item);

            // 扣减库存
            goodsMapper.decreaseStock(goods.getId(), cart.getQuantity());
        }

        // 清空已结算的购物车
        cartMapper.deleteChecked(userId);

        order.setItems(orderItemMapper.findByOrderId(order.getId()));
        return order;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer maxSeq = orderMapper.getMaxOrderNoSeq(dateStr);
        int seq = (maxSeq == null ? 0 : maxSeq) + 1;
        return "LY" + dateStr + String.format("%04d", seq);
    }

    private String generateVerifyCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }

    public void cancel(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待取货的订单");
        }
        orderMapper.updateStatus(id, "cancelled");
    }

    @Transactional
    public void verify(Long orderId, Long adminId, String adminName) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常，无法核销");
        }

        orderMapper.verify(orderId);

        VerifyLog log = new VerifyLog();
        log.setOrderId(orderId);
        log.setOrderNo(order.getOrderNo());
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setAction("verify");
        log.setRemark("核销成功");
        verifyLogMapper.insert(log);
    }

    @Transactional
    public void adminCancel(Long orderId, Long adminId, String adminName, String reason) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if ("completed".equals(order.getStatus())) {
            throw new RuntimeException("已完成的订单无法取消");
        }

        orderMapper.updateStatus(orderId, "cancelled");

        VerifyLog log = new VerifyLog();
        log.setOrderId(orderId);
        log.setOrderNo(order.getOrderNo());
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setAction("cancel");
        log.setRemark(reason != null ? reason : "后台取消");
        verifyLogMapper.insert(log);
    }
}



