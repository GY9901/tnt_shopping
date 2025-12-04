package com.example.tnt_shopping_background.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String content;

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;
}