package com.example.tnt_shopping_background.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String role;

    // 新增：映射数据库的 create_time 字段
    // insertable=false, updatable=false 表示该字段由数据库自动维护(默认值)，JPA不插入不更新
    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;
}