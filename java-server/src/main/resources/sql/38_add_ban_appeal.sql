-- 创建禁言申诉表
-- 日期: 2026-06-04
-- 描述: 添加禁言申诉功能所需的数据库表和菜单

USE java_2_48;

-- 1. 创建 bbs_ban_appeal 表
CREATE TABLE IF NOT EXISTS `bbs_ban_appeal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申诉ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `reason` VARCHAR(500) NOT NULL COMMENT '申诉理由',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理, 1=已通过, 2=已驳回',
    `handler_id` BIGINT NULL COMMENT '处理人ID',
    `handle_result` VARCHAR(500) NULL COMMENT '处理结果',
    `handle_time` VARCHAR(30) NULL COMMENT '处理时间',
    `create_time` VARCHAR(30) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禁言申诉表';

-- 2. 在帮助菜单中添加禁言申诉菜单项
INSERT INTO menu (id, pid, name, title, user_type_ids) 
VALUES (507, 405, 'ban_appeal', '禁言申诉', '1,2,3')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    title = VALUES(title),
    user_type_ids = VALUES(user_type_ids);

-- 3. 在管理员菜单中添加禁言申诉管理菜单项
INSERT INTO menu (id, pid, name, title, user_type_ids) 
VALUES (508, 5, 'ban_appeal_admin', '禁言申诉管理', '1,2')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    title = VALUES(title),
    user_type_ids = VALUES(user_type_ids);

-- 验证数据
SELECT * FROM bbs_ban_appeal;
SELECT * FROM menu WHERE id IN (507, 508);

-- 完成！
