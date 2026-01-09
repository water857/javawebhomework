USE smart_community;

CREATE TABLE IF NOT EXISTS `private_message` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `from_user_id` INT NOT NULL,
    `to_user_id` INT NOT NULL,
    `content` TEXT NOT NULL,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `is_read` TINYINT(1) DEFAULT 0,
    FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `private_message` (`from_user_id`, `to_user_id`, `content`, `is_read`) VALUES
(1, 4, '你好，想咨询一下小区健身房开放时间。', 0),
(4, 1, '晚上 18:00-21:00 开放哦。', 1);
