package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Product;
import com.example.tnt_shopping_background.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // 获取商品列表 (分页)
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "4") Integer size) {

        // 构建分页对象 (Spring Data JPA 页码从0开始，所以前端传1要减1)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        Page<Product> pageResult;
        if (category != null && !category.isEmpty()) {
            pageResult = productRepository.findByCategory(category, pageable);
        } else {
            pageResult = productRepository.findAll(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getContent());
        data.put("total", pageResult.getTotalElements());

        return Result.success(data);
    }
}