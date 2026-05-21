-- ============================================================
-- 34_add_draft_box.sql
-- 多草稿存储的草稿箱功能
-- ============================================================

CREATE TABLE IF NOT EXISTS bbs_draft (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    title VARCHAR(256) DEFAULT '' COMMENT '帖子标题',
    board_id BIGINT DEFAULT NULL COMMENT '板块ID',
    board_name VARCHAR(100) DEFAULT NULL COMMENT '板块名称',
    content TEXT COMMENT '帖子内容',
    image_urls TEXT COMMENT '图片URL列表(JSON数组)',
    attachment_infos TEXT COMMENT '附件信息(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子草稿表';

-- 在个人中心(pid=300)下添加"草稿箱"菜单项
INSERT IGNORE INTO menu (id, user_type_ids, pid, name, title)
VALUES (204, '1,2,3', 200, 'DraftBox', '草稿箱');
