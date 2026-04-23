-- ============================================
-- 创建违禁词表 bbs_sensitive_word
-- ============================================
CREATE TABLE IF NOT EXISTS bbs_sensitive_word (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '违禁词 ID',
    word VARCHAR(50) NOT NULL UNIQUE COMMENT '违禁词内容',
    level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '违禁等级（1 = 普通违规，替换为 ***；2 = 严重违规，自动下架）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='违禁词表';

-- 插入测试数据 - 普通违规（level=1）
INSERT INTO bbs_sensitive_word (word, level) VALUES 
('敏感词1', 1),
('敏感词2', 1),
('敏感词3', 1),
('敏感词4', 1),
('敏感词5', 1);

-- 插入测试数据 - 严重违规（level=2）
INSERT INTO bbs_sensitive_word (word, level) VALUES 
('严重违规1', 2),
('严重违规2', 2),
('严重违规3', 2),
('严重违规4', 2),
('严重违规5', 2);

-- ============================================
-- 创建举报表 bbs_report
-- ============================================
CREATE TABLE IF NOT EXISTS bbs_report (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '举报 ID',
    reporter_id BIGINT NOT NULL COMMENT '举报者 ID',
    target_type TINYINT(1) NOT NULL COMMENT '举报对象类型（1 = 帖子，2 = 评论）',
    target_id BIGINT NOT NULL COMMENT '举报对象 ID',
    reason VARCHAR(500) NOT NULL COMMENT '举报理由',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '举报状态（0 = 待处理，1 = 已处理）',
    handler_id BIGINT NULL DEFAULT NULL COMMENT '处理人 ID',
    handle_type TINYINT(1) NULL DEFAULT NULL COMMENT '处理方式（1 = 删除内容，2 = 驳回举报）',
    handle_remark VARCHAR(200) NULL DEFAULT NULL COMMENT '处理备注',
    handle_time DATETIME NULL DEFAULT NULL COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_target_type (target_type),
    INDEX idx_target_id (target_id),
    INDEX idx_status (status),
    INDEX idx_handler_id (handler_id),
    INDEX idx_handle_type (handle_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ============================================
-- 创建通知表 bbs_notification
-- ============================================
CREATE TABLE IF NOT EXISTS bbs_notification (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '通知 ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者 ID',
    content VARCHAR(500) NOT NULL COMMENT '通知内容',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0 = 未读，1 = 已读）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
