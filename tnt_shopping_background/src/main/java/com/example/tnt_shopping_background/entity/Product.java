package com.example.tnt_shopping_background.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "sys_product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String category; // 棉花娃娃 / 小卡
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
}