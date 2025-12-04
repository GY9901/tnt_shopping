package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.CartItem;
import com.example.tnt_shopping_background.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    // 获取购物车列表
    @GetMapping("/list")
    public Result<List<CartItem>> list(@RequestParam String username) {
        return Result.success(cartItemRepository.findByUsername(username));
    }

    // 添加商品到购物车
    @PostMapping("/add")
    public Result<?> add(@RequestBody CartItem cartItem) {
        // 1. 检查该用户是否已添加过该商品
        CartItem existItem = cartItemRepository.findByUsernameAndProductId(cartItem.getUsername(), cartItem.getProductId());
        
        if (existItem != null) {
            // 2. 如果存在，数量增加
            existItem.setQuantity(existItem.getQuantity() + (cartItem.getQuantity() == null ? 1 : cartItem.getQuantity()));
            cartItemRepository.save(existItem);
        } else {
            // 3. 如果不存在，新增记录
            if (cartItem.getQuantity() == null) {
                cartItem.setQuantity(1);
            }
            cartItemRepository.save(cartItem);
        }
        return Result.success(null);
    }

    // 更新数量
    @PostMapping("/update")
    public Result<?> update(@RequestBody CartItem cartItem) {
        CartItem dbItem = cartItemRepository.findById(cartItem.getId()).orElse(null);
        if (dbItem != null) {
            dbItem.setQuantity(cartItem.getQuantity());
            cartItemRepository.save(dbItem);
        }
        return Result.success(null);
    }

    // 删除购物车商品
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        cartItemRepository.deleteById(id);
        return Result.success(null);
    }
}