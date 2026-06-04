-- ===============================================================
-- 创建禁言申诉表
-- ===============================================================

USE java_2_48;

-- 创建 bbs_ban_appeal 表
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
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禁言申诉表';

-- 验证表是否创建成功
SHOW TABLES LIKE 'bbs_ban_appeal';

-- 查看表结构
DESC bbs_ban_appeal;

-- ===============================================================
-- 添加禁言申诉相关菜单（如果还没有的话）
-- ===============================================================

-- 检查帮助菜单的 id，假设是 405
-- 为了安全起见，先查看菜单表中现有的帮助菜单
SELECT id, pid, name, title FROM menu WHERE title LIKE '%帮助%' OR name LIKE '%help%';

-- 然后手动添加菜单。如果菜单已经通过其他方式管理，跳过此部分。

-- 完成！
