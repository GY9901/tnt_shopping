package com.example.tnt_shopping_background.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "sys_order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 修改点：改为对象关联，使用 @ManyToOne
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore // 防止序列化时死循环
    private Order order;

    private String productName;
    private String productImg;
    private BigDecimal price;
    private Integer quantity;
}