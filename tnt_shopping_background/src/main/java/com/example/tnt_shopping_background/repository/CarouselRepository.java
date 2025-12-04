package com.example.tnt_shopping_background.repository;

import com.example.tnt_shopping_background.entity.Carousel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarouselRepository extends JpaRepository<Carousel, Integer> {
    // 查询启用的轮播图并按排序字段排序
    List<Carousel> findByStatusOrderBySortAsc(Integer status);
    
    // 如果想按区域查也可以加: findBySectionAndStatusOrderBySortAsc
}