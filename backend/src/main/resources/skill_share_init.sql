USE smart_community;

CREATE TABLE IF NOT EXISTS `skill_share` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `skill_name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `contact` VARCHAR(100),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `skill_share` (`user_id`, `skill_name`, `description`, `contact`) VALUES
(1, '钢琴陪练', '可提供周末 1 小时陪练，适合初级。', '张三 13800138001'),
(4, '摄影指导', '手机摄影取景和修图指导，欢迎交流。', '李四 13800138004');
