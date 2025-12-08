package com.example.tnt_shopping_background.repository;

import com.example.tnt_shopping_background.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // 修改为支持分页查询 (返回 Page 对象)
    Page<Order> findByUsernameOrderByCreateTimeDesc(String username, Pageable pageable);
}