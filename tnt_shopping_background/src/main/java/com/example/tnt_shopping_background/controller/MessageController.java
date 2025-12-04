package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Message;
import com.example.tnt_shopping_background.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    // 获取留言列表
    @GetMapping("/list")
    public Result<List<Message>> list() {
        return Result.success(messageRepository.findAllByOrderByCreateTimeAsc());
    }

    // 发布留言
    @PostMapping("/add")
    public Result<?> add(@RequestBody Message message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return Result.error("400", "留言内容不能为空");
        }
        messageRepository.save(message);
        return Result.success(null);
    }
}