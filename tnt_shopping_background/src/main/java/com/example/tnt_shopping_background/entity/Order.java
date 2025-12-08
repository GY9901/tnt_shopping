package com.example.tnt_shopping_background.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "sys_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderNo;
    private String username;
    private BigDecimal totalAmount;
    private String status;

    // 新增字段：支付方式
    private String paymentMethod;

    @Column(name = "create_time", insertable = false, updatable = false)
    private Date createTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}