
-- ===============================================================
-- AI内容审核功能数据库表设计脚本
-- 执行前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 扩展 bbs_post 表，添加审核相关字段
-- ===============================================================

-- 添加审核状态字段
-- 如果报错"Duplicate column name"，说明字段已存在，请注释掉这一行
ALTER TABLE bbs_post ADD COLUMN moderation_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态：pending=审核中，pass=审核通过，reject=内容违规，manual=待人工审核' AFTER status;

-- 添加审核结果字段
ALTER TABLE bbs_post ADD COLUMN moderation_violation_level VARCHAR(20) DEFAULT NULL COMMENT '违规等级：serious=严重，normal=一般，none=无' AFTER moderation_status;

-- 添加违规类型字段
ALTER TABLE bbs_post ADD COLUMN moderation_violation_type VARCHAR(100) DEFAULT NULL COMMENT '违规类型' AFTER moderation_violation_level;

-- 添加违规片段字段
ALTER TABLE bbs_post ADD COLUMN moderation_violation_fragments TEXT DEFAULT NULL COMMENT '违规片段（JSON数组）' AFTER moderation_violation_type;

-- 添加审核建议字段
ALTER TABLE bbs_post ADD COLUMN moderation_suggestion TEXT DEFAULT NULL COMMENT '审核建议' AFTER moderation_violation_fragments;

-- 添加审核置信度字段
ALTER TABLE bbs_post ADD COLUMN moderation_confidence INT DEFAULT NULL COMMENT '审核置信度（0-100）' AFTER moderation_suggestion;

-- 添加审核备注字段
ALTER TABLE bbs_post ADD COLUMN moderation_remark TEXT DEFAULT NULL COMMENT '审核备注' AFTER moderation_confidence;

-- 添加审核时间字段
ALTER TABLE bbs_post ADD COLUMN moderation_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER moderation_remark;

-- 添加审核人ID字段
ALTER TABLE bbs_post ADD COLUMN moderator_id INT DEFAULT NULL COMMENT '审核人ID（管理员ID）' AFTER moderation_time;

-- 添加索引
ALTER TABLE bbs_post ADD INDEX idx_moderation_status (moderation_status);
ALTER TABLE bbs_post ADD INDEX idx_moderation_time (moderation_time);

-- ===============================================================
-- 2. 创建审核历史记录表 bbs_moderation_log（可选，用于审计）
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_moderation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核日志ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    moderator_id INT DEFAULT NULL COMMENT '审核人ID（NULL表示AI审核）',
    old_status VARCHAR(20) DEFAULT NULL COMMENT '变更前状态',
    new_status VARCHAR(20) NOT NULL COMMENT '变更后状态',
    violation_level VARCHAR(20) DEFAULT NULL COMMENT '违规等级',
    violation_type VARCHAR(100) DEFAULT NULL COMMENT '违规类型',
    remark TEXT DEFAULT NULL COMMENT '审核备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_post_id (post_id),
    INDEX idx_moderator_id (moderator_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核历史记录表';

-- ===============================================================
-- 3. 为现有帖子初始化审核状态（可选）
-- ===============================================================
-- 将现有帖子状态设为已审核通过（兼容历史数据）
UPDATE bbs_post SET moderation_status = 'pass', moderation_time = NOW() WHERE moderation_status IS NULL OR moderation_status = '';

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看bbs_post表结构
DESC bbs_post;

-- 查看bbs_moderation_log表结构
DESC bbs_moderation_log;

-- 查看索引
SHOW INDEX FROM bbs_post;
SHOW INDEX FROM bbs_moderation_log;

-- 查看审核状态分布
SELECT moderation_status, COUNT(*) as count FROM bbs_post GROUP BY moderation_status;
