USE smart_community;

CREATE TABLE IF NOT EXISTS `visitor_record` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `visitor_name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20),
    `visit_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `host_user_id` INT NOT NULL,
    `status` VARCHAR(20) DEFAULT 'registered',
    FOREIGN KEY (`host_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `visitor_record` (`visitor_name`, `phone`, `host_user_id`, `status`) VALUES
('王老师', '13900001111', 1, 'registered'),
('快递员赵师傅', '13900002222', 4, 'completed');
