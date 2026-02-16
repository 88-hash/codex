package com.leyi.service;

import com.leyi.entity.Comment;
import com.leyi.entity.OrderItem;
import com.leyi.mapper.CommentMapper;
import com.leyi.mapper.OrderItemMapper;
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

    public List<Comment> getByGoodsId(Long goodsId) {
        return commentMapper.findByGoodsId(goodsId);
    }

    public List<Comment> getByUserId(Long userId) {
        return commentMapper.findByUserId(userId);
    }

    @Transactional
    public void add(Comment comment) {
        OrderItem item = orderItemMapper.findById(comment.getOrderItemId());
        if (item == null) {
            throw new RuntimeException("订单明细不存在");
        }
        if (item.getIsCommented() == 1) {
            throw new RuntimeException("该商品已评价");
        }

        Comment existing = commentMapper.findByOrderItemId(comment.getOrderItemId());
        if (existing != null) {
            throw new RuntimeException("该商品已评价");
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



