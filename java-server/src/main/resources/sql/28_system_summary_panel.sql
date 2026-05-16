-- =====================================================
-- 系统概览界面 SQL 更新记录
-- 版本: v1.0
-- 日期: 2026-05-14
-- 说明: 记录系统概览界面开发过程中的数据库变更
-- =====================================================

-- =====================================================
-- 注意：
-- 本次开发不需要修改数据库表结构
-- 后端统计 API 已完善，前端直接从现有接口获取数据
-- =====================================================

-- =====================================================
-- 以下是可选的优化建议（如需要）
-- =====================================================

-- 1. 为统计数据添加索引（优化查询性能）
-- CREATE INDEX idx_user_create_time ON user(create_time);
-- CREATE INDEX idx_bbs_post_create_time ON bbs_post(create_time);
-- CREATE INDEX idx_bbs_post_status ON bbs_post(status);

-- 2. 创建统计汇总表（可选，用于预计算统计数据）
-- CREATE TABLE IF NOT EXISTS statistics_summary (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     stat_date DATE NOT NULL,
--     stat_type VARCHAR(50) NOT NULL,
--     stat_value BIGINT DEFAULT 0,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     UNIQUE KEY uk_date_type (stat_date, stat_type)
-- );

-- 3. 创建系统配置表（可选，用于系统设置）
-- CREATE TABLE IF NOT EXISTS system_config (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     config_key VARCHAR(100) NOT NULL UNIQUE,
--     config_value TEXT,
--     description VARCHAR(255),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- );

-- =====================================================
-- 后续开发记录
-- =====================================================

-- 2026-05-14: 系统概览界面 v1.0
-- - 前端: SystemSummaryController.java
-- - 界面: system_summary_panel.fxml
-- - 样式: system-summary.css
-- - 功能: 智能仪表盘 + AI 监控中心
-- - 数据来源: StatisticsService 已有 API

