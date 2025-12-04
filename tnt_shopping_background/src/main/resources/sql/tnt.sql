-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS tnt_shop DEFAULT CHARSET utf8mb4;

USE tnt_shop;

-- 2. 创建用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                            `password` VARCHAR(100) NOT NULL COMMENT '密码',
                            `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: admin/user',
                            `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 初始化管理员账号 (admin/admin)
INSERT INTO `sys_user` (`username`, `password`, `role`) VALUES ('admin', 'admin', 'admin');

-- 轮播图管理表
DROP TABLE IF EXISTS `sys_carousel`;
CREATE TABLE `sys_carousel` (
                                `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `section` VARCHAR(20) NOT NULL COMMENT '区域: MAIN(顶部)/EXCLUSIVE(左上)/HOT(左下)',
                                `title` VARCHAR(100) DEFAULT NULL COMMENT '主标题',
                                `sub_title` VARCHAR(100) DEFAULT NULL COMMENT '副标题',
                                `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片地址(若为空则显示纯色背景)',
                                `bg_color` VARCHAR(20) DEFAULT '#1A1A1A' COMMENT '背景色(无图时使用)',
                                `sort` INT DEFAULT 0 COMMENT '排序(越小越前)',
                                `status` TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
                                `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图配置表';

-- 初始化测试数据 (模拟每个区域4张图)
INSERT INTO `sys_carousel` (`section`, `title`, `sub_title`, `bg_color`, `sort`) VALUES
-- 顶部大轮播 (MAIN)
('MAIN', 'TNT NEW ARRIVAL 1', '2025 SUMMER COLLECTION', '#FAD02C', 1),
('MAIN', 'TNT CONCERT LIVE', 'WORLD TOUR 2025', '#FAD02C', 2),
('MAIN', 'NEW ALBUM RELEASE', 'LIMITED EDITION', '#FAD02C', 3),
('MAIN', 'MEMBER SPECIAL', 'DOCUMENTARY', '#FAD02C', 4),

-- 左上独家花絮 (EXCLUSIVE)
('EXCLUSIVE', 'BEHIND THE SCENE 1', NULL, '#1A1A1A', 1),
('EXCLUSIVE', 'DANCE PRACTICE', NULL, '#1A1A1A', 2),
('EXCLUSIVE', 'VLOG DAILY', NULL, '#1A1A1A', 3),
('EXCLUSIVE', 'MV MAKING', NULL, '#1A1A1A', 4),

-- 左下热销单品 (HOT)
('HOT', 'TOP SELLING 1', NULL, '#FFFFFF', 1),
('HOT', 'OFFICIAL LIGHT STICK', NULL, '#FFFFFF', 2),
('HOT', 'PHOTO CARD SET', NULL, '#FFFFFF', 3),
('HOT', 'HOODIE BLACK', NULL, '#FFFFFF', 4);

-- 留言板表
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
                               `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `username` VARCHAR(50) NOT NULL COMMENT '留言用户',
                               `content` VARCHAR(500) NOT NULL COMMENT '留言内容',
                               `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户留言表';


-- 商品表
DROP TABLE IF EXISTS `sys_product`;
CREATE TABLE `sys_product` (
                               `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
                               `category` VARCHAR(50) NOT NULL COMMENT '分类: 棉花娃娃/小卡',
                               `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
                               `stock` INT DEFAULT 0 COMMENT '库存',
                               `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片地址',
                               `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 初始化测试数据 (扩充数据量以支持分页测试，每页4条)
INSERT INTO `sys_product` (`name`, `category`, `price`, `stock`, `image_url`) VALUES
-- 棉花娃娃 (第一页)
('宋亚轩 20cm 娃衣套装', '棉花娃娃', 128.00, 50, NULL),
('马嘉祺 官方应援娃', '棉花娃娃', 158.00, 30, NULL),
('严浩翔 舞台限定娃', '棉花娃娃', 148.00, 45, NULL),
('张真源 饭制可爱娃', '棉花娃娃', 98.00, 100, NULL),
-- 棉花娃娃 (第二页测试数据)
('刘耀文 狼崽公仔', '棉花娃娃', 120.00, 60, NULL),
('贺峻霖 兔子玩偶', '棉花娃娃', 110.00, 80, NULL),
('丁程鑫 狐狸抱枕', '棉花娃娃', 88.00, 200, NULL),
('全员 Q版手办套装', '棉花娃娃', 399.00, 20, NULL),

-- 小卡 (第一页)
('全员 Summer 拍立得小卡', '小卡', 35.00, 200, NULL),
('刘耀文 舞台限定小卡', '小卡', 40.00, 100, NULL),
('贺峻霖 签售会限定卡', '小卡', 55.00, 80, NULL),
('丁程鑫 杂志内页小卡', '小卡', 30.00, 150, NULL),
-- 小卡 (第二页测试数据)
('宋亚轩 拍立得复刻', '小卡', 25.00, 300, NULL),
('马嘉祺 演唱会限定卡', '小卡', 45.00, 120, NULL),
('张真源 训练生时期卡', '小卡', 15.00, 50, NULL),
('严浩翔 杂志特典卡', '小卡', 38.00, 90, NULL);

-- 购物车表
DROP TABLE IF EXISTS `sys_cart`;
CREATE TABLE `sys_cart` (
                            `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                            `product_id` INT NOT NULL COMMENT '商品ID',
                            `quantity` INT DEFAULT 1 COMMENT '数量',
                            `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
    -- 联合唯一索引，确保同一个用户对同一个商品只有一条记录
                            UNIQUE KEY `uk_user_product` (`username`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';