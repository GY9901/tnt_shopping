package com.example.tnt_shopping_background.repository;

import com.example.tnt_shopping_background.entity.Message;
import com.example.tnt_shopping_background.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    // 查询所有留言，按时间倒序排列 (最新的在前面，或者正序排列看需求，通常留言板是下面是最新的)
    // 这里我们按时间正序，最新的显示在底部
    List<Message> findAllByOrderByCreateTimeAsc();
    Page<Message> findByUsernameContaining(String username, Pageable pageable);
}