USE smart_community;

-- 活动表
CREATE TABLE IF NOT EXISTS `activities` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '发布用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '活动标题',
    `content` TEXT NOT NULL COMMENT '活动内容',
    `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
    `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
    `location` VARCHAR(255) NOT NULL COMMENT '活动地点',
    `max_participants` INT DEFAULT 0 COMMENT '最大参与人数（0表示无限制）',
    `current_participants` INT DEFAULT 0 COMMENT '当前参与人数',
    `status` ENUM('pending', 'ongoing', 'completed', 'cancelled') DEFAULT 'pending' COMMENT '活动状态：pending-待开始，ongoing-进行中，completed-已完成，cancelled-已取消',
    `qr_code_url` VARCHAR(255) COMMENT '签到二维码URL',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动参与者表
CREATE TABLE IF NOT EXISTS `activity_participants` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `user_id` INT NOT NULL COMMENT '参与者用户ID',
    `status` ENUM('registered', 'attended', 'absent') DEFAULT 'registered' COMMENT '参与状态：registered-已报名，attended-已签到，absent-未签到',
    `registration_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `checkin_time` TIMESTAMP COMMENT '签到时间',
    `evaluation` TEXT COMMENT '活动评价',
    `rating` INT CHECK (rating BETWEEN 1 AND 5) COMMENT '活动评分（1-5分）',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 活动图片表（可选，用于活动回顾）
CREATE TABLE IF NOT EXISTS `activity_images` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `user_id` INT NOT NULL COMMENT '上传用户ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `activities` (`user_id`, `title`, `content`, `start_time`, `end_time`, `location`, `max_participants`, `current_participants`, `status`) VALUES
(2, '邻里运动日', '本周六早晨举办社区健身活动，欢迎报名。', '2024-09-20 08:30:00', '2024-09-20 10:30:00', '社区广场', 30, 2, 'pending'),
(2, '家庭安全讲座', '邀请消防人员分享家庭安全与应急知识。', '2024-09-25 19:00:00', '2024-09-25 20:30:00', '活动中心会议室', 50, 1, 'pending');

INSERT INTO `activity_participants` (`activity_id`, `user_id`, `status`) VALUES
(1, 1, 'registered'),
(1, 4, 'registered'),
(2, 1, 'registered');
