package com.example.tnt_shopping_background.repository;

import com.example.tnt_shopping_background.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    // 根据分类查询商品 (支持分页)
    Page<Product> findByCategory(String category, Pageable pageable);
}