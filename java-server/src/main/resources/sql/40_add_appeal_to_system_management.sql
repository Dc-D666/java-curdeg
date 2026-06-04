-- ===============================================================
-- 添加申诉管理到系统管理菜单下
-- ===============================================================

USE java_2_48;

-- 先删除可能存在的旧记录
DELETE FROM menu WHERE id = 509;

-- 添加申诉管理菜单到系统管理（id=500）下
INSERT INTO menu (id, pid, name, title, user_type_ids) 
VALUES (509, 500, 'ban_appeal_admin', '申诉管理', '1,2');

-- 验证是否添加成功
SELECT * FROM menu WHERE id = 509;

-- 查看系统管理的所有子菜单
SELECT * FROM menu WHERE pid = 500;

-- 完成！
