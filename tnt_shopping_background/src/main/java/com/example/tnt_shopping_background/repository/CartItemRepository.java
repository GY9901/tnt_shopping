package com.example.tnt_shopping_background.repository;

import com.example.tnt_shopping_background.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    // 查询某用户的购物车列表
    List<CartItem> findByUsername(String username);

    // 查询某用户购物车中是否存在特定商品
    CartItem findByUsernameAndProductId(String username, Integer productId);
}