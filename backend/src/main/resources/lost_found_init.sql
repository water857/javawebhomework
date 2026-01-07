USE smart_community;

CREATE TABLE IF NOT EXISTS `lost_and_found` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `type` VARCHAR(10) NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `contact` VARCHAR(100),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
