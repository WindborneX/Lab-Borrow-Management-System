-- 实验室设备借用管理系统 - 数据库初始化脚本 (V1)
-- 遵循 AGENT_INSTRUCTIONS.md 数据库设计原则

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部唯一ID',
    `student_id` VARCHAR(20) NOT NULL UNIQUE COMMENT '学号/工号',
    `username` VARCHAR(50) NOT NULL COMMENT '姓名',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-学生, 1-管理员',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 2. 设备表
CREATE TABLE IF NOT EXISTS `equipment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部唯一ID',
    `name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `category` VARCHAR(50) COMMENT '分类 (如: 开发板, 传感器)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-在库, 1-借出, 2-维修, 3-停用',
    `description` TEXT COMMENT '设备详情/备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备信息表';

-- 3. 借用记录表 (遵循可追溯原则)
CREATE TABLE IF NOT EXISTS `borrow_records` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部唯一ID',
    `equipment_id` BIGINT NOT NULL COMMENT '关联设备ID',
    `user_id` BIGINT NOT NULL COMMENT '关联借用人ID',
    `borrow_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '实际借出时间',
    `expect_return_time` DATETIME COMMENT '预计归还时间',
    `actual_return_time` DATETIME COMMENT '实际归还时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '记录状态: 0-借用中, 1-已归还, 2-超期未还',
    CONSTRAINT `fk_record_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`),
    CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    INDEX `idx_equipment_id` (`equipment_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_record_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借用历史记录表';

-- 初始测试数据
INSERT INTO `users` (`student_id`, `username`, `role`) VALUES 
('ADMIN001', '系统管理员', 1),
('20260001', '张三', 0);

INSERT INTO `equipment` (`name`, `category`, `description`) VALUES 
('Jetson Nano B01', '开发板', '实验室竞赛用核心板'),
('树莓派 4B (8G)', '开发板', '自带散热外壳'),
('单目摄像头模块', '传感器', '720P 分辨率'),
('多功能工具箱', '工具', '含电烙铁与万用表');
