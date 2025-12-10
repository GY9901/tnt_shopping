package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Product;
import com.example.tnt_shopping_background.entity.User;
import com.example.tnt_shopping_background.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<Product> pageResult;
        if (name != null && !name.isEmpty() && category != null && !category.isEmpty()) {
            pageResult = productRepository.findByNameContainingAndCategoryContaining(name, category, pageable);
        } else if (name != null && !name.isEmpty()) {
            pageResult = productRepository.findByNameContaining(name, pageable);
        } else if (category != null && !category.isEmpty()) {
            pageResult = productRepository.findByCategoryContaining(category, pageable);
        } else {
            pageResult = productRepository.findAll(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getContent());
        data.put("total", pageResult.getTotalElements());

        return Result.success(data);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteProduct(@PathVariable Integer id) {
        productRepository.deleteById(id);
        return Result.success(null);
    }

    @PutMapping("/admin/update")
    public Result<?> adminUpdateProduct(@RequestBody Product product) {
        productRepository.save(product);
        return Result.success(null);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        return Result.success(productRepository.findById(id).orElse(null));
    }

    @PostMapping("/add")
    public Result<?> register(@RequestBody Product user) {
        Product dbProduct = productRepository.findByName(user.getName());
        if (dbProduct != null) {
            return Result.error("400", "商品已存在");
        }
        // 默认注册为普通用户
        productRepository.save(user);
        return Result.success(null);
    }
}