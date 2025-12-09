package com.example.tnt_shopping_background.repository;

import com.example.tnt_shopping_background.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    Page<User> findByUsernameContaining(String username, Pageable pageable);
}