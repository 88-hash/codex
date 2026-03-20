package com.leyi.service;

import com.leyi.entity.Comment;
import com.leyi.entity.Order;
import com.leyi.entity.OrderItem;
import com.leyi.exception.BusinessException;
import com.leyi.mapper.CommentMapper;
import com.leyi.mapper.OrderItemMapper;
import com.leyi.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    public List<Comment> getByGoodsId(Long goodsId) {
        return commentMapper.findByGoodsId(goodsId);
    }

    public List<Comment> getByUserId(Long userId) {
        return commentMapper.findByUserId(userId);
    }

    @Transactional
    public void add(Long currentUserId, Comment comment) {
        if (comment.getOrderItemId() == null) {
            throw new BusinessException(400, "订单明细不能为空");
        }
        if (comment.getRating() == null || comment.getRating() < 1 || comment.getRating() > 5) {
            throw new BusinessException(400, "评分必须在1到5之间");
        }

        OrderItem item = orderItemMapper.findById(comment.getOrderItemId());
        if (item == null) {
            throw new BusinessException(400, "订单明细不存在");
        }
        if (item.getIsCommented() == 1) {
            throw new BusinessException(400, "该商品已评价");
        }

        Order order = orderMapper.findById(item.getOrderId());
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        if (!currentUserId.equals(order.getUserId())) {
            throw new BusinessException(403, "无权评价该订单");
        }
        if (!"completed".equals(order.getStatus())) {
            throw new BusinessException(400, "订单完成后才能评价");
        }

        Comment existing = commentMapper.findByOrderItemId(comment.getOrderItemId());
        if (existing != null) {
            throw new BusinessException(400, "该商品已评价");
        }

        comment.setGoodsId(item.getGoodsId());
        comment.setGoodsName(item.getGoodsName());
        commentMapper.insert(comment);
        orderItemMapper.markCommented(item.getId());
    }

    public Double getAverageRating(Long goodsId) {
        return commentMapper.getAverageRating(goodsId);
    }

    public int getCount(Long goodsId) {
        return commentMapper.countByGoodsId(goodsId);
    }
}
