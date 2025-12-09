package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.User;
import com.example.tnt_shopping_background.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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


    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        return Result.success(userRepository.findById(id).orElse(null));
    }

    // --- 管理员接口 ---

    /**
     * 获取用户列表，支持根据用户名模糊搜索和分页
     * @param username (可选) 用户名关键词
     * @param pageNum (可选) 页码，默认1
     * @param pageSize (可选) 每页大小，默认10
     * @return 分页后的用户列表
     */
    @GetMapping("/all")
    public Result<?> getAllUsers(@RequestParam(required = false) String username,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        // 构建分页对象，注意 Spring Data JPA 的页码是从 0 开始的，所以要减 1
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<User> page;
        if (username != null && !username.isEmpty()) {
            // 如果提供了 username 参数，则进行模糊查询 + 分页
            page = userRepository.findByUsernameContaining(username, pageable);
        } else {
            // 否则查询所有 + 分页
            page = userRepository.findAll(pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        return Result.success(data);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return Result.success(null);
    }

    @PutMapping("/admin/update")
    public Result<?> adminUpdateUser(@RequestBody User user) {
        userRepository.save(user);
        return Result.success(null);
    }
}