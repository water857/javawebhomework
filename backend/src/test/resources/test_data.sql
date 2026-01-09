USE smart_community;

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `id_card`, `address`, `role`) VALUES
(1, 'zhangsan', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '张三', '13800138001', 'zhangsan@example.com', '110101199001011234', '1号楼1单元101室', 'resident'),
(2, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', '13800138002', 'admin@example.com', '110101199001015678', '物业办公室', 'property_admin'),
(3, 'service1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '服务商1', '13800138003', 'service1@example.com', '110101199001019012', '服务中心', 'service_provider');
