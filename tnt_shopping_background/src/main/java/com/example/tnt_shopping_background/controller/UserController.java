package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.User;
import com.example.tnt_shopping_background.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}