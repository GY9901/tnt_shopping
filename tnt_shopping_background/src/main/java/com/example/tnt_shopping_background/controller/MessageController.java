package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Message;
import com.example.tnt_shopping_background.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    // 获取所有留言 (用于首页)
    @GetMapping("/list")
    public Result<List<Message>> list() {
        return Result.success(messageRepository.findAllByOrderByCreateTimeAsc());
    }

    // 获取"我的"留言 (用于个人中心)
    @GetMapping("/my")
    public Result<List<Message>> myMessages(@RequestParam String username) {
        // 简单实现：获取所有然后过滤，或者在 Repository 加个 findByUsername
        List<Message> all = messageRepository.findAllByOrderByCreateTimeAsc();
        List<Message> myMsgs = all.stream()
                .filter(m -> m.getUsername().equals(username))
                .collect(Collectors.toList());
        return Result.success(myMsgs);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Message message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return Result.error("400", "留言内容不能为空");
        }
        messageRepository.save(message);
        return Result.success(null);
    }
}