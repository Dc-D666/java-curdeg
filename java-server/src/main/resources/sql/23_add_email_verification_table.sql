-- ===============================================================
-- 邮箱验证码功能数据库表设计脚本
-- 执行前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 创建邮箱验证码表 email_verification
-- ===============================================================
CREATE TABLE IF NOT EXISTS email_verification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    email VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    verification_code VARCHAR(10) NOT NULL COMMENT '验证码',
    verification_type VARCHAR(20) NOT NULL DEFAULT 'REGISTER' COMMENT '验证类型：REGISTER=注册，RESET_PASSWORD=重置密码',
    send_count INT NOT NULL DEFAULT 1 COMMENT '发送次数',
    verify_count INT NOT NULL DEFAULT 0 COMMENT '验证尝试次数',
    is_verified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已验证：0-未验证，1-已验证',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_email_type (email, verification_type),
    INDEX idx_expire_time (expire_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看email_verification表结构
DESC email_verification;

-- 查看索引
SHOW INDEX FROM email_verification;
