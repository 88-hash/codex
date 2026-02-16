package com.leyi.controller;

import com.leyi.common.PageResult;
import com.leyi.common.Result;
import com.leyi.entity.Goods;
import com.leyi.service.CommentService;
import com.leyi.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isOnSale,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Goods> result = goodsService.getList(categoryId, keyword, isOnSale, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("goods", goods);
        result.put("comments", commentService.getByGoodsId(id));
        result.put("avgRating", commentService.getAverageRating(id));
        result.put("commentCount", commentService.getCount(id));
        return Result.success(result);
    }

    @GetMapping("/barcode/{barCode}")
    public Result<?> getByBarCode(@PathVariable String barCode) {
        Goods goods = goodsService.getByBarCode(barCode);
        return Result.success(goods);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Object> params) {
        Goods goods = new Goods();
        goods.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        goods.setName((String) params.get("name"));
        goods.setPrice(new java.math.BigDecimal(params.get("price").toString()));
        goods.setStock(Integer.valueOf(params.get("stock").toString()));
        goods.setSafetyStock(params.get("safetyStock") != null ? Integer.valueOf(params.get("safetyStock").toString()) : 10);
        goods.setBarCode((String) params.get("barCode"));
        goods.setImageUrl((String) params.get("imageUrl"));
        goods.setDescription((String) params.get("description"));
        goods.setPromotionTag((String) params.get("promotionTag"));
        goods.setIsOnSale(params.get("isOnSale") != null ? Integer.valueOf(params.get("isOnSale").toString()) : 1);
        if (params.get("expireDate") != null && !params.get("expireDate").toString().isEmpty()) {
            goods.setExpireDate(java.time.LocalDate.parse(params.get("expireDate").toString()));
        }
        
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) params.get("imageUrls");
        goodsService.add(goods, imageUrls);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Map<String, Object> params) {
        Goods goods = new Goods();
        goods.setId(Long.valueOf(params.get("id").toString()));
        goods.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        goods.setName((String) params.get("name"));
        goods.setPrice(new java.math.BigDecimal(params.get("price").toString()));
        goods.setStock(Integer.valueOf(params.get("stock").toString()));
        goods.setSafetyStock(params.get("safetyStock") != null ? Integer.valueOf(params.get("safetyStock").toString()) : 10);
        goods.setBarCode((String) params.get("barCode"));
        goods.setImageUrl((String) params.get("imageUrl"));
        goods.setDescription((String) params.get("description"));
        goods.setPromotionTag((String) params.get("promotionTag"));
        goods.setIsOnSale(params.get("isOnSale") != null ? Integer.valueOf(params.get("isOnSale").toString()) : 1);
        if (params.get("expireDate") != null && !params.get("expireDate").toString().isEmpty()) {
            goods.setExpireDate(java.time.LocalDate.parse(params.get("expireDate").toString()));
        }
        
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) params.get("imageUrls");
        goodsService.update(goods, imageUrls);
        return Result.success();
    }

    @PutMapping("/sale/{id}")
    public Result<?> updateSaleStatus(@PathVariable Long id, @RequestParam Integer isOnSale) {
        goodsService.updateSaleStatus(id, isOnSale);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        goodsService.delete(id);
        return Result.success();
    }
}



