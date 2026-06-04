-- ===============================================================
-- 最简单的只建表SQL - 创建禁言申诉表
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
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禁言申诉表';

-- 验证表是否创建成功
SHOW TABLES LIKE 'bbs_ban_appeal';

-- 查看表结构
DESC bbs_ban_appeal;

-- 完成！
