USE smart_community;

-- 物业费账单表
CREATE TABLE IF NOT EXISTS `property_fee_bill` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '居民用户ID',
    `period_start` DATE NOT NULL COMMENT '费用周期开始日期',
    `period_end` DATE NOT NULL COMMENT '费用周期结束日期',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
    `status` ENUM('unpaid', 'paid', 'overdue', 'partially_paid') DEFAULT 'unpaid' COMMENT '账单状态：unpaid-未支付，paid-已支付，overdue-逾期，partially_paid-部分支付',
    `items` JSON NOT NULL COMMENT '费用项目明细，JSON格式：[{"name": "物业费", "amount": 100.00}, {"name": "水费", "amount": 50.00}]',
    `due_date` DATE NOT NULL COMMENT '缴费截止日期',
    `payment_deadline_reminder_sent` BOOLEAN DEFAULT FALSE COMMENT '是否已发送缴费提醒',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_period` (`period_start`, `period_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 物业费支付记录表
CREATE TABLE IF NOT EXISTS `property_fee_payment` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `bill_id` INT NOT NULL COMMENT '关联的账单ID',
    `user_id` INT NOT NULL COMMENT '支付用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `payment_method` ENUM('wechat', 'alipay', 'bank_transfer', 'auto_deduction') NOT NULL COMMENT '支付方式：wechat-微信支付，alipay-支付宝，bank_transfer-银行转账，auto_deduction-自动扣费',
    `transaction_id` VARCHAR(100) NOT NULL COMMENT '支付渠道交易ID',
    `status` ENUM('pending', 'success', 'failed', 'refunded') DEFAULT 'pending' COMMENT '支付状态：pending-待处理，success-成功，failed-失败，refunded-已退款',
    `payment_time` TIMESTAMP COMMENT '支付时间',
    `auto_deduction` BOOLEAN DEFAULT FALSE COMMENT '是否为自动扣费',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`bill_id`) REFERENCES `property_fee_bill`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_bill_id` (`bill_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_payment_time` (`payment_time`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 物业费配置表
CREATE TABLE IF NOT EXISTS `property_fee_config` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '配置项名称',
    `value` JSON NOT NULL COMMENT '配置项值，JSON格式',
    `description` VARCHAR(255) COMMENT '配置项描述',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_config_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始配置数据
INSERT INTO `property_fee_config` (`name`, `value`, `description`) VALUES
('payment_methods', '["wechat", "alipay", "auto_deduction"]', '支持的支付方式'),
('reminder_settings', '{"before_due_days": [7, 3, 1], "overdue_days": [1, 7, 15]}', '缴费提醒设置'),
('auto_deduction_settings', '{"enabled": true, "deduction_day": 1, "max_attempts": 3}', '自动扣费设置'),
('bill_items', '[{"name": "物业费", "type": "fixed"}, {"name": "水费", "type": "meter"}, {"name": "电费", "type": "meter"}, {"name": "垃圾处理费", "type": "fixed"}]', '费用项目配置');
