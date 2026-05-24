-- ===============================================================
-- 数据库清理脚本
-- 仅删除课程管理相关的表，保留基础框架的核心表
-- ===============================================================

USE java_2_48;

-- ===============================================================
-- 删除不需要的课程管理表（按依赖关系顺序）
-- ===============================================================

DROP TABLE IF EXISTS student_statistics;
DROP TABLE IF EXISTS student_leave;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS score;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS family_member;
DROP TABLE IF EXISTS fee;
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS modify_log;
DROP TABLE IF EXISTS request_log;
DROP TABLE IF EXISTS system_info;
DROP TABLE IF EXISTS dictionary;

-- ===============================================================
-- 保留的基础表（不要删除！）
-- ===============================================================
-- person        - 人员信息表
-- user          - 用户账号表
-- user_type     - 用户类型表
-- statistics_day - 日统计表（可选保留）

-- ===============================================================
-- 验证清理结果
-- ===============================================================
SHOW TABLES;
