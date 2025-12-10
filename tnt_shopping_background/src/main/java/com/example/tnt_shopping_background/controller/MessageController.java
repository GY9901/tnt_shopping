package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Message;
import com.example.tnt_shopping_background.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    // 管理员获取评论列表（分页）
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Message> messagePage;
        
        // 这里可以添加按用户名筛选的功能
        if (username != null && !username.isEmpty()) {
            messagePage = messageRepository.findByUsernameContaining(username, pageable);
        } else {
            messagePage = messageRepository.findAll(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", messagePage.getContent());
        data.put("total", messagePage.getTotalElements());
        
        return Result.success(data);
    }
    
    // 管理员删除评论
    @DeleteMapping("/admin/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        try {
            messageRepository.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "删除评论失败: " + e.getMessage());
        }
    }
}