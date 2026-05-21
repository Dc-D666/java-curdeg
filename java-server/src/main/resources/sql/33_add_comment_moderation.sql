-- ============================================================
-- 33_add_comment_moderation.sql
-- 将AI内容审核能力扩展至评论区
-- ============================================================

-- 1. bbs_comment 表新增审核相关字段
ALTER TABLE bbs_comment
    ADD COLUMN moderation_status VARCHAR(20) NOT NULL DEFAULT 'pass' COMMENT '审核状态: pending/pass/reject/manual' AFTER status,
    ADD COLUMN moderation_violation_level VARCHAR(20) DEFAULT NULL COMMENT '违规等级: serious/normal/none' AFTER moderation_status,
    ADD COLUMN moderation_violation_type VARCHAR(100) DEFAULT NULL COMMENT '违规类型' AFTER moderation_violation_level,
    ADD COLUMN moderation_violation_fragments TEXT DEFAULT NULL COMMENT '违规片段(JSON数组)' AFTER moderation_violation_type,
    ADD COLUMN moderation_suggestion TEXT DEFAULT NULL COMMENT '修改建议' AFTER moderation_violation_fragments,
    ADD COLUMN moderation_confidence INT DEFAULT NULL COMMENT '审核置信度 0-100' AFTER moderation_suggestion,
    ADD COLUMN moderation_remark TEXT DEFAULT NULL COMMENT '审核备注' AFTER moderation_confidence,
    ADD COLUMN moderation_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER moderation_remark,
    ADD COLUMN moderator_id INT DEFAULT NULL COMMENT '人工审核管理员ID' AFTER moderation_time,
    ADD INDEX idx_comment_moderation_status (moderation_status);

-- 2. 历史评论全部标记为审核通过
UPDATE bbs_comment SET moderation_status='pass', moderation_time=NOW() WHERE moderation_status IS NULL OR moderation_status='';
