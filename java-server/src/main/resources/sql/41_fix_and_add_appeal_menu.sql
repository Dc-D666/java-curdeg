-- ===============================================================
-- 修复并添加申诉管理到系统管理
-- ===============================================================

USE java_2_48;

-- 1. 先查看当前menu表结构
DESC menu;

-- 2. 修复系统管理菜单的user_type_ids（原来是3，改为1,2）
UPDATE menu SET user_type_ids = '1,2' WHERE id = 500;

-- 3. 先删除可能存在的旧记录
DELETE FROM menu WHERE id = 509;

-- 4. 添加申诉管理菜单到系统管理（id=500）下
-- 使用驼峰命名BanAppealAdmin，会自动转换为ban-appeal-admin
INSERT INTO menu (id, pid, name, title, user_type_ids) 
VALUES (509, 500, 'BanAppealAdmin', '申诉管理', '1,2');

-- 5. 验证是否添加成功
SELECT * FROM menu WHERE id = 509;

-- 6. 查看系统管理的所有子菜单
SELECT * FROM menu WHERE pid = 500;

-- 7. 查看系统管理菜单本身
SELECT * FROM menu WHERE id = 500;

-- 完成！
