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
