USE smart_community;

CREATE TABLE IF NOT EXISTS `activity_review` (
    `activity_id` INT PRIMARY KEY,
    `summary` TEXT,
    `images` TEXT,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
