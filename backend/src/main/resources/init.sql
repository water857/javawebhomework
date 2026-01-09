-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_community;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `id_card` VARCHAR(18) COMMENT '身份证号',
    `address` VARCHAR(255) COMMENT '地址',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：resident-居民，property_admin-物业管理员，service_provider-服务商',
    `status` INT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 社区公告表
CREATE TABLE IF NOT EXISTS `announcement` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `author_id` INT NOT NULL COMMENT '发布者ID',
    `author_name` VARCHAR(50) NOT NULL COMMENT '发布者名称',
    `status` INT DEFAULT 1 COMMENT '状态：1-发布，0-草稿',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `published_at` TIMESTAMP NULL DEFAULT NULL COMMENT '发布时间',
    FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `id_card`, `address`, `role`) VALUES
('zhangsan', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '张三', '13800138001', 'zhangsan@example.com', '110101199001011234', '1号楼1单元101室', 'resident'),
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', '13800138002', 'admin@example.com', '110101199001015678', '物业办公室', 'property_admin'),
('service1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '服务商1', '13800138003', 'service1@example.com', '110101199001019012', '服务中心', 'service_provider'),
('lisi', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '李四', '13800138004', 'lisi@example.com', '110101199101012345', '2号楼2单元202室', 'resident');

-- 插入测试公告数据
INSERT INTO `announcement` (`title`, `content`, `author_id`, `author_name`, `status`, `published_at`) VALUES
('社区安全通知', '近期小区内发生多起电动车被盗事件，请各位居民加强防范，将电动车停放在指定停车棚并上锁。', 2, '管理员', 1, CURRENT_TIMESTAMP),
('物业费缴纳通知', '请各位居民及时缴纳本月物业费，缴纳截止日期为下月5日。', 2, '管理员', 1, CURRENT_TIMESTAMP);

-- 导入物业费相关表结构和初始数据
SOURCE property_fee_init.sql;
SOURCE activities_init.sql;
SOURCE neighborhood_circle_init.sql;
SOURCE second_hand_init.sql;
SOURCE lost_found_init.sql;
SOURCE skill_share_init.sql;
SOURCE parking_init.sql;
SOURCE visitor_init.sql;
SOURCE private_message_init.sql;
SOURCE repair_init.sql;
