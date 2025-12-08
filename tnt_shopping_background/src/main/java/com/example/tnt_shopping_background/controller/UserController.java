package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.User;
import com.example.tnt_shopping_background.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        User dbUser = userRepository.findByUsername(user.getUsername());
        if (dbUser == null) {
            return Result.error("401", "用户不存在");
        }
        if (!dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("401", "密码错误");
        }
        return Result.success(dbUser);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        User dbUser = userRepository.findByUsername(user.getUsername());
        if (dbUser != null) {
            return Result.error("400", "用户名已存在");
        }
        // 默认注册为普通用户
        if (user.getRole() == null) {
            user.setRole("user");
        }
        userRepository.save(user);
        return Result.success(null);
    }

    // 更新用户信息 (支持修改密码)
    @PostMapping("/update")
    public Result<?> update(@RequestBody Map<String, Object> payload) {
        Integer id = (Integer) payload.get("id");
        String oldPassword = (String) payload.get("oldPassword");
        String newPassword = (String) payload.get("password");

        if (id == null) {
            return Result.error("400", "用户ID不能为空");
        }

        User dbUser = userRepository.findById(id).orElse(null);
        if (dbUser == null) {
            return Result.error("404", "用户不存在");
        }

        // 如果提供了旧密码，则进行校验（用于修改密码场景）
        if (oldPassword != null && !oldPassword.isEmpty()) {
            if (!dbUser.getPassword().equals(oldPassword)) {
                return Result.error("401", "当前密码错误");
            }
        }

        // 修改新密码
        if (newPassword != null && !newPassword.isEmpty()) {
            dbUser.setPassword(newPassword);
        }

        userRepository.save(dbUser);
        return Result.success(dbUser);
    }
}