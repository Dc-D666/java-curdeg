-- ===============================================================
-- 为通知表添加 type 和 title 字段
-- ===============================================================

USE java_2_48;

-- 添加 type 字段（通知类型：1=系统通知，2=举报处理通知，3=帖子审核通知，4=评论回复通知）
ALTER TABLE bbs_notification
ADD COLUMN type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '通知类型（1=系统通知，2=举报处理通知，3=帖子审核通知，4=评论回复通知）' AFTER receiver_id,
ADD INDEX idx_type (type);

-- 添加 title 字段（通知标题）
ALTER TABLE bbs_notification
ADD COLUMN title VARCHAR(100) DEFAULT NULL COMMENT '通知标题' AFTER type,
ADD INDEX idx_title (title);

-- 为现有通知设置默认标题
UPDATE bbs_notification 
SET title = '系统通知' 
WHERE title IS NULL;
