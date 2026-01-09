USE smart_community;

CREATE TABLE IF NOT EXISTS `lost_and_found` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `type` VARCHAR(10) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `contact` VARCHAR(100),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `lost_and_found` (`user_id`, `type`, `title`, `description`, `contact`) VALUES
(1, 'lost', '丢失蓝色雨伞', '上周日晚在南门停车棚附近丢失蓝色折叠伞。', '张三 13800138001'),
(4, 'found', '拾到门禁卡', '在 2 号楼大厅捡到一张门禁卡。', '李四 13800138004');
