-- 报修表
CREATE TABLE IF NOT EXISTS `repair` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `resident_id` INT NOT NULL COMMENT '居民ID',
    `title` VARCHAR(100) NOT NULL COMMENT '报修标题',
    `content` TEXT NOT NULL COMMENT '报修内容',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待受理，processing-处理中，completed-已完成',
    `assigned_to` INT COMMENT '分配给的服务商ID',
    `property_admin_id` INT COMMENT '处理的物业管理员ID',
    `repair_time` DATETIME COMMENT '维修时间',
    `completed_time` DATETIME COMMENT '完成时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`resident_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`assigned_to`) REFERENCES `user`(`id`),
    FOREIGN KEY (`property_admin_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 报修图片表
CREATE TABLE IF NOT EXISTS `repair_image` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `repair_id` INT NOT NULL COMMENT '报修ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`repair_id`) REFERENCES `repair`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 报修评价表
CREATE TABLE IF NOT EXISTS `repair_evaluation` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `repair_id` INT NOT NULL COMMENT '报修ID',
    `resident_id` INT NOT NULL COMMENT '居民ID',
    `service_provider_id` INT NOT NULL COMMENT '服务商ID',
    `rating` INT NOT NULL COMMENT '评分：1-5分',
    `comment` TEXT COMMENT '评价内容',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`repair_id`) REFERENCES `repair`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`resident_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`service_provider_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `uk_repair_evaluation_repair_id` (`repair_id`) -- 确保每个报修单只能有一条评价
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;