-- ===============================================================
-- 私信功能数据库表设计脚本
-- 功能：
-- 1. 创建私信会话表 bbs_conversation
-- 2. 创建私信消息表 bbs_message
-- 3. 添加必要的索引
-- 使用前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 创建私信会话表 bbs_conversation
-- 用于存储两个用户之间的会话关系
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
-- 2. 创建私信消息表 bbs_message
-- 用于存储具体的私信消息内容
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
    INDEX idx_is_read (is_read),
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES bbs_conversation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- ===============================================================
-- 3. 验证脚本
-- ===============================================================

-- 查看bbs_conversation表结构
DESC bbs_conversation;

-- 查看bbs_message表结构
DESC bbs_message;

-- 查看bbs_conversation表索引
SHOW INDEX FROM bbs_conversation;

-- 查看bbs_message表索引
SHOW INDEX FROM bbs_message;

-- ===============================================================
-- 测试查询（可选）
-- ===============================================================

-- 测试查询某个用户的所有会话
-- SELECT * FROM bbs_conversation WHERE user1_id = 1 OR user2_id = 1 ORDER BY last_message_time DESC;

-- 测试查询某个会话的所有消息
-- SELECT * FROM bbs_message WHERE conversation_id = 1 ORDER BY create_time ASC;

-- ===============================================================
-- 完成！
-- ===============================================================
