-- ===============================================================
-- 查看菜单结构，找到系统管理的正确ID
-- ===============================================================

USE java_2_48;

-- 查看所有顶级菜单（pid为null或0）
SELECT * FROM menu WHERE pid IS NULL OR pid = 0;

-- 查看所有菜单，找出系统管理相关的
SELECT * FROM menu WHERE title LIKE '%系统%' OR name LIKE '%system%' OR name LIKE '%admin%';

-- 查看前20个菜单，了解结构
SELECT * FROM menu LIMIT 20;

-- 完成！
