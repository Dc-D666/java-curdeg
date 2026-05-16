-- ===============================================================
-- 私信功能数据库表诊断和修复脚本
-- 功能：
-- 1. 检查并创建缺失的表
-- 2. 添加缺失的字段
-- 3. 添加缺失的索引
-- 使用前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 检查并创建 bbs_conversation 表
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_conversation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    user1_id INT NOT NULL COMMENT '用户1ID（较小者）',
    user2_id INT NOT NULL COMMENT '用户2ID（较大者）',
    last_message_id BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
    last_message_time VARCHAR(20) DEFAULT NULL COMMENT '最后消息时间',
    user1_unread_count INT NOT NULL DEFAULT 0 COMMENT '用户1未读消息数',
    user2_unread_count INT NOT NULL DEFAULT 0 COMMENT '用户2未读消息数',
    create_time VARCHAR(20) NOT NULL COMMENT '创建时间',
    update_time VARCHAR(20) NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_pair (user1_id, user2_id),
    INDEX idx_user1_id (user1_id),
    INDEX idx_user2_id (user2_id),
    INDEX idx_last_message_time (last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信会话表';

-- ===============================================================
-- 2. 检查并创建 bbs_message 表
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    conversation_id BIGINT NOT NULL COMMENT '所属会话ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    receiver_id INT NOT NULL COMMENT '接收者ID',
    message_type VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '消息类型：text-文字，image-图片',
    content TEXT COMMENT '消息内容（文字内容或图片URL）',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    create_time VARCHAR(20) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_create_time (create_time),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- ===============================================================
-- 3. 添加外键约束（如果不存在）
-- ===============================================================
-- 检查并添加外键约束
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE
                 WHERE TABLE_SCHEMA = 'java_2_48' AND TABLE_NAME = 'bbs_message' 
                 AND CONSTRAINT_NAME = 'fk_message_conversation');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE bbs_message ADD CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES bbs_conversation(id) ON DELETE CASCADE',
    'SELECT "外键约束 fk_message_conversation 已存在" AS status');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===============================================================
-- 4. 验证并检查表结构
-- ===============================================================
SELECT '=== 检查 bbs_conversation 表结构 ===' AS info;
DESC bbs_conversation;

SELECT '=== 检查 bbs_message 表结构 ===' AS info;
DESC bbs_message;

SELECT '=== 检查 bbs_conversation 表索引 ===' AS info;
SHOW INDEX FROM bbs_conversation;

SELECT '=== 检查 bbs_message 表索引 ===' AS info;
SHOW INDEX FROM bbs_message;

-- ===============================================================
-- 5. 检查表数据
-- ===============================================================
SELECT '=== bbs_conversation 表数据 ===' AS info;
SELECT * FROM bbs_conversation;

SELECT '=== bbs_message 表数据 ===' AS info;
SELECT * FROM bbs_message;

-- ===============================================================
-- 完成！
-- ===============================================================
