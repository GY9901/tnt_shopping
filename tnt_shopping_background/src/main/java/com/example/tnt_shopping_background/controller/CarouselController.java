package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Carousel;
import com.example.tnt_shopping_background.repository.CarouselRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/carousel")
public class CarouselController {

    @Autowired
    private CarouselRepository carouselRepository;

    // 获取首页所有轮播图
    @GetMapping("/list")
    public Result<Map<String, List<Carousel>>> list() {
        // 1. 获取所有状态为1(启用)的轮播图
        List<Carousel> allBanners = carouselRepository.findByStatusOrderBySortAsc(1);
        
        // 2. 按 section 分组 (Java 8 Stream API)
        Map<String, List<Carousel>> groupedBanners = allBanners.stream()
                .collect(Collectors.groupingBy(Carousel::getSection));
        
        return Result.success(groupedBanners);
    }
    
    // 管理员添加/修改轮播图接口 (预留)
    @PostMapping("/save")
    public Result<?> save(@RequestBody Carousel carousel) {
        carouselRepository.save(carousel);
        return Result.success(null);
    }
    
    // 管理员删除接口 (预留)
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        carouselRepository.deleteById(id);
        return Result.success(null);
    }
}