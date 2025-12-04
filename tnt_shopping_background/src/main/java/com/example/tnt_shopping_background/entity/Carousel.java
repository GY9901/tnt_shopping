package com.example.tnt_shopping_background.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_carousel")
public class Carousel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String section; // MAIN, EXCLUSIVE, HOT
    private String title;
    private String subTitle;
    private String imageUrl;
    private String bgColor;
    private Integer sort;
    private Integer status;
}