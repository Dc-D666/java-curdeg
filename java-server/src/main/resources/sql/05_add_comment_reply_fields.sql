-- ===============================================================
-- 为评论表添加回复关系字段
-- 执行前请确保已备份数据库！
-- ===============================================================

USE java_2_48;

-- ===============================================================
-- 为 bbs_comment 表添加回复关系字段
-- ===============================================================
ALTER TABLE bbs_comment
ADD COLUMN reply_to_comment_id BIGINT NULL DEFAULT NULL COMMENT '被回复的评论ID' AFTER parent_id,
ADD COLUMN reply_to_user_id BIGINT NULL DEFAULT NULL COMMENT '被回复的用户ID' AFTER reply_to_comment_id,
ADD COLUMN reply_to_user_nickname VARCHAR(50) NULL DEFAULT NULL COMMENT '被回复的用户昵称' AFTER reply_to_user_id,
ADD INDEX idx_reply_to_comment_id (reply_to_comment_id),
ADD INDEX idx_reply_to_user_id (reply_to_user_id);

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看bbs_comment表结构，确认新字段已添加
DESC bbs_comment;

-- 查看索引
SHOW INDEX FROM bbs_comment;
