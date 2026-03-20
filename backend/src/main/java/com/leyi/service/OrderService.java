package com.leyi.service;

import com.leyi.common.PageResult;
import com.leyi.entity.*;
import com.leyi.exception.BusinessException;
import com.leyi.mapper.*;
import com.leyi.util.OrderIdentityGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private static final int MAX_IDENTITY_RETRY = 5;

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

    @Autowired
    private OrderIdentityGenerator orderIdentityGenerator;

    public List<Order> getByUserId(Long userId, String status) {
        List<Order> orders = orderMapper.findByUserId(userId, status);
        orders.forEach(this::loadItems);
        return orders;
    }

    public PageResult<Order> getList(String status, String keyword, Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Order> list = orderMapper.findList(status, keyword, offset, pageSize);
        list.forEach(this::loadItems);
        Long total = orderMapper.countList(status, keyword);
        return PageResult.of(list, total, pageNum, pageSize);
    }

    public Order getById(Long id) {
        Order order = orderMapper.findById(id);
        if (order != null) {
            loadItems(order);
        }
        return order;
    }

    public Order getByIdForUser(Long id, Long userId) {
        Order order = orderMapper.findByIdAndUserId(id, userId);
        if (order == null) {
            throw new BusinessException(403, "无权访问订单");
        }
        loadItems(order);
        return order;
    }

    public Order getByVerifyCode(String verifyCode) {
        Order order = orderMapper.findByVerifyCode(verifyCode);
        if (order != null) {
            loadItems(order);
        }
        return order;
    }

    @Transactional
    public Order create(Long userId, String remark) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在或登录已失效");
        }

        List<Cart> cartItems = cartMapper.findCheckedByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(400, "请选择要结算的商品");
        }

        cartItems.sort(Comparator.comparing(Cart::getGoodsId));

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            Integer quantity = cart.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new BusinessException(400, "购物车商品数量异常，请返回购物车调整");
            }

            Goods goods = goodsMapper.findById(cart.getGoodsId());
            if (goods == null || goods.getIsOnSale() != 1) {
                throw new BusinessException(400, "商品 " + cart.getGoodsName() + " 已下架");
            }
            if (goods.getStock() == null || goods.getStock() <= 0) {
                throw new BusinessException(400, "商品 " + goods.getName() + " 库存不足");
            }
            if (quantity > goods.getStock()) {
                throw new BusinessException(400, "商品 " + goods.getName() + " 库存不足");
            }
            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        Order order = createOrderShell(userId, user.getPhone(), totalPrice, remark);

        for (Cart cart : cartItems) {
            Goods goods = goodsMapper.findById(cart.getGoodsId());
            int quantity = cart.getQuantity();
            BigDecimal subtotal = goods.getPrice().multiply(BigDecimal.valueOf(quantity));

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setGoodsId(goods.getId());
            item.setGoodsName(goods.getName());
            item.setGoodsImage(goods.getImageUrl());
            item.setPrice(goods.getPrice());
            item.setQuantity(quantity);
            item.setSubtotal(subtotal);
            orderItemMapper.insert(item);

            int decreased = goodsMapper.decreaseStock(goods.getId(), quantity);
            if (decreased != 1) {
                throw new BusinessException(400, "商品 " + goods.getName() + " 库存不足，请返回购物车重试");
            }
        }

        cartMapper.deleteChecked(userId);
        loadItems(order);
        return order;
    }

    @Transactional
    public void cancelByUser(Long id, Long userId) {
        Order order = orderMapper.findByIdAndUserId(id, userId);
        if (order == null) {
            throw new BusinessException(403, "无权操作该订单");
        }
        cancelPendingOrder(order, null, null, null);
    }

    @Transactional
    public void verify(Long orderId, Long adminId, String adminName) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException(400, "订单状态异常，无法核销");
        }

        int updated = orderMapper.verifyPending(orderId);
        if (updated != 1) {
            throw new BusinessException(400, "订单状态已变化，请刷新后重试");
        }

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
            throw new BusinessException(400, "订单不存在");
        }
        cancelPendingOrder(order, adminId, adminName, reason);
    }

    private Order createOrderShell(Long userId, String userPhone, BigDecimal totalPrice, String remark) {
        for (int i = 0; i < MAX_IDENTITY_RETRY; i++) {
            Order order = new Order();
            order.setUserId(userId);
            order.setUserPhone(userPhone);
            order.setOrderNo(orderIdentityGenerator.nextOrderNo());
            order.setTotalPrice(totalPrice);
            order.setStatus("pending");
            order.setVerifyCode(orderIdentityGenerator.nextVerifyCode());
            order.setRemark(remark == null ? "" : remark);
            try {
                orderMapper.insert(order);
                return order;
            } catch (DuplicateKeyException e) {
                if (i == MAX_IDENTITY_RETRY - 1) {
                    throw new BusinessException(400, "订单生成失败，请重试");
                }
            }
        }
        throw new BusinessException(400, "订单生成失败，请重试");
    }

    private void cancelPendingOrder(Order order, Long adminId, String adminName, String reason) {
        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException(400, "只有待取货订单可以取消");
        }

        int updated = orderMapper.updateStatusIfCurrent(order.getId(), "pending", "cancelled");
        if (updated != 1) {
            throw new BusinessException(400, "订单状态已变化，请刷新后重试");
        }

        restoreStock(order.getId());

        if (adminId != null) {
            VerifyLog log = new VerifyLog();
            log.setOrderId(order.getId());
            log.setOrderNo(order.getOrderNo());
            log.setAdminId(adminId);
            log.setAdminName(adminName);
            log.setAction("cancel");
            log.setRemark(reason == null || reason.trim().isEmpty() ? "后台取消" : reason.trim());
            verifyLogMapper.insert(log);
        }
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            Integer quantity = item.getQuantity();
            if (quantity == null || quantity <= 0) {
                throw new BusinessException(400, "订单明细数量异常，无法回补库存");
            }
            int increased = goodsMapper.increaseStock(item.getGoodsId(), quantity);
            if (increased != 1) {
                throw new BusinessException(400, "库存回补失败，请检查商品数据");
            }
        }
    }

    private void loadItems(Order order) {
        order.setItems(orderItemMapper.findByOrderId(order.getId()));
    }
}
