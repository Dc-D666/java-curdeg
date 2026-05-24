-- ===============================================================
-- 用户浏览历史功能数据库表设计脚本
-- 执行前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 创建浏览历史表 bbs_user_browse_history
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_user_browse_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '浏览记录ID',
    user_id INT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    browse_time VARCHAR(20) NOT NULL COMMENT '浏览时间',
    duration_seconds INT DEFAULT 0 COMMENT '浏览时长（秒）',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_browse_time (browse_time),
    UNIQUE KEY uk_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户浏览历史表';

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看表结构
DESC bbs_user_browse_history;

-- 查看索引
SHOW INDEX FROM bbs_user_browse_history;
