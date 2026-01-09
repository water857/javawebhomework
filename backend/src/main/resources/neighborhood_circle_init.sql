USE smart_community;

-- 邻里圈动态表
CREATE TABLE IF NOT EXISTS `community_posts` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '发布用户ID',
    `content` TEXT COMMENT '动态内容',
    `privacy` ENUM('public', 'specific') DEFAULT 'public' COMMENT '隐私设置：public-公开，specific-指定邻居可见',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 动态图片表
CREATE TABLE IF NOT EXISTS `post_images` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `post_id` INT NOT NULL COMMENT '动态ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `community_posts`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 点赞表
CREATE TABLE IF NOT EXISTS `post_likes` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `post_id` INT NOT NULL COMMENT '动态ID',
    `user_id` INT NOT NULL COMMENT '点赞用户ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    FOREIGN KEY (`post_id`) REFERENCES `community_posts`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 评论表
CREATE TABLE IF NOT EXISTS `post_comments` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `post_id` INT NOT NULL COMMENT '动态ID',
    `user_id` INT NOT NULL COMMENT '评论用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` INT DEFAULT NULL COMMENT '父评论ID（回复评论时使用）',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `community_posts`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `post_comments`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE IF NOT EXISTS `tags` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 动态标签关联表
CREATE TABLE IF NOT EXISTS `post_tags` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `post_id` INT NOT NULL COMMENT '动态ID',
    `tag_id` INT NOT NULL COMMENT '标签ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
    FOREIGN KEY (`post_id`) REFERENCES `community_posts`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 通知表
CREATE TABLE IF NOT EXISTS `notifications` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '接收通知用户ID',
    `type` ENUM('like', 'comment', 'system') NOT NULL COMMENT '通知类型：like-点赞，comment-评论，system-系统通知',
    `related_id` INT COMMENT '关联ID（如点赞ID、评论ID）',
    `content` VARCHAR(255) NOT NULL COMMENT '通知内容',
    `is_read` BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 指定邻居可见关系表
CREATE TABLE IF NOT EXISTS `post_visible_users` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `post_id` INT NOT NULL COMMENT '动态ID',
    `user_id` INT NOT NULL COMMENT '可见用户ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_visible_user` (`post_id`, `user_id`),
    FOREIGN KEY (`post_id`) REFERENCES `community_posts`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始标签数据
INSERT INTO `tags` (`name`) VALUES
('生活分享'),
('社区活动'),
('求助问答'),
('二手交易'),
('社区公告'),
('美食推荐'),
('宠物天地'),
('安全提示');

INSERT INTO `community_posts` (`user_id`, `content`, `privacy`, `like_count`, `comment_count`, `view_count`) VALUES
(1, '今天小区花园里的桂花开了，香味特别好闻～', 'public', 2, 1, 15),
(4, '求推荐附近靠谱的家政服务，欢迎邻居私信。', 'public', 1, 0, 8);

INSERT INTO `post_comments` (`post_id`, `user_id`, `content`) VALUES
(1, 4, '闻到了！晚上散步特别舒服。');

INSERT INTO `notifications` (`user_id`, `type`, `content`, `is_read`) VALUES
(1, 'comment', '李四评论了你的动态：闻到了！晚上散步特别舒服。', false);

INSERT INTO `post_tags` (`post_id`, `tag_id`) VALUES
(1, 1),
(2, 3);
