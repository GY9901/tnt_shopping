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
INSERT INTO `sys_carousel` (`section`, `image_url`, `sort`) VALUES
-- 顶部大轮播 (MAIN)
('MAIN', '/images/adv/轮播1-1.jpg', 1),
('MAIN', '/images/adv/轮播1-2.jpg', 2),
('MAIN', '/images/adv/轮播1-3.jpg', 3),
('MAIN', '/images/adv/轮播1-4.jpg', 4),

-- 左上独家花絮 (EXCLUSIVE)
('EXCLUSIVE', '/images/adv/轮播2-1.jpg', 1),
('EXCLUSIVE', '/images/adv/轮播2-2.jpg', 2),
('EXCLUSIVE', '/images/adv/轮播2-3.jpg', 3),
('EXCLUSIVE', '/images/adv/轮播2-4.jpg', 4),

-- 左下热销单品 (HOT)
('HOT', '/images/adv/轮播3-1.jpg', 1),
('HOT', '/images/adv/轮播3-2.jpg', 2),
('HOT', '/images/adv/轮播3-3.jpg', 3),
('HOT', '/images/adv/轮播3-4.jpg', 4);

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
('棉花娃娃商品图1', '棉花娃娃', 79.00, 50, '/images/棉花娃娃商品图1.jpg'),
('棉花娃娃商品图2', '棉花娃娃', 59.00, 30, '/images/棉花娃娃商品图2.jpg'),
('棉花娃娃商品图3', '棉花娃娃', 59.00, 45, '/images/棉花娃娃商品图3.jpg'),
('棉花娃娃商品图5', '棉花娃娃', 69.00, 100, '/images/棉花娃娃商品图4.jpg'),
('小卡商品图1', '小卡', 70.00, 60, '/images/小卡商品图1.jpg'),
('小卡商品图2', '小卡', 70.00, 80, '/images/小卡商品图2.jpg'),
('小卡商品图3', '小卡', 80.00, 200, '/images/小卡商品图3.jpg'),
('小卡商品图4', '小卡', 80.00, 20, '/images/小卡商品图4.jpg');

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

-- 订单主表
DROP TABLE IF EXISTS `sys_order`;
CREATE TABLE `sys_order` (
                             `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
                             `username` VARCHAR(50) NOT NULL COMMENT '下单用户',
                             `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
                             `status` VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态: PENDING/COMPLETED/CANCELLED',
                             `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单商品详情表
DROP TABLE IF EXISTS `sys_order_item`;
CREATE TABLE `sys_order_item` (
                                  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `order_id` INT NOT NULL COMMENT '关联订单ID',
                                  `product_name` VARCHAR(100) COMMENT '商品名称快照',
                                  `product_img` VARCHAR(255) COMMENT '图片快照',
                                  `price` DECIMAL(10,2) COMMENT '购买单价',
                                  `quantity` INT COMMENT '购买数量',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- 订单主表
DROP TABLE IF EXISTS `sys_order`;
CREATE TABLE `sys_order` (
                             `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
                             `username` VARCHAR(50) NOT NULL COMMENT '下单用户',
                             `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
                             `payment_method` VARCHAR(20) DEFAULT 'ALIPAY' COMMENT '支付方式: ALIPAY/WECHAT',
                             `status` VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态',
                             `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单商品详情表
DROP TABLE IF EXISTS `sys_order_item`;
CREATE TABLE `sys_order_item` (
                                  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `order_id` INT NOT NULL COMMENT '关联订单ID',
                                  `product_name` VARCHAR(100) COMMENT '商品名称快照',
                                  `product_img` VARCHAR(255) COMMENT '图片快照',
                                  `price` DECIMAL(10,2) COMMENT '购买单价',
                                  `quantity` INT COMMENT '购买数量',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';