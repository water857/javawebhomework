USE smart_community;

CREATE TABLE IF NOT EXISTS `second_hand_item` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10,2) DEFAULT 0,
    `status` VARCHAR(20) DEFAULT 'available',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `second_hand_item` (`user_id`, `title`, `description`, `price`, `status`) VALUES
(1, '九成新电饭煲', '仅用两次，功能完好。', 120.00, 'available'),
(4, '书架转让', '原木色书架，成色八成。', 80.00, 'available'),
(1, '儿童自行车', '适合 6-8 岁儿童，已消毒。', 150.00, 'sold');
