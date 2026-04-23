Exception in Application start method
java.lang.reflect.InvocationTargetException
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:118)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at javafx.graphics@21/com.sun.javafx.application.LauncherImpl.launchApplicationWithArgs(LauncherImpl.java:464)
	at javafx.graphics@21/com.sun.javafx.application.LauncherImpl.launchApplication(LauncherImpl.java:364)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at java.base/sun.launcher.LauncherHelper$FXHelper.main(LauncherHelper.java:1156)
Caused by: java.lang.RuntimeException: Exception in Application start method
	at javafx.graphics@21/com.sun.javafx.application.LauncherImpl.launchApplication1(LauncherImpl.java:893)
	at javafx.graphics@21/com.sun.javafx.application.LauncherImpl.lambda$launchApplication$2(LauncherImpl.java:196)
	at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: javafx.fxml.LoadException: CheckBox is not a valid type.
/C:/Users/33031/Documents/xwechat_files/wxid_0bowm1ekyr2s22_5e3e/msg/file/2026-04/java-curdeg(2)/java-curdeg/java-curdeg/java-fx/target/classes/com/teach/javafx/base/login-view.fxml:49
-- 收藏帖子功能 - 数据库变更脚本
-- 执行日期: 2026-04-11

-- ============================================
-- 1. 创建收藏表 bbs_favorite
-- ============================================
CREATE TABLE IF NOT EXISTS bbs_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id INT NOT NULL COMMENT '用户ID',
    create_time VARCHAR(255) COMMENT '创建时间',
    UNIQUE KEY uk_post_user (post_id, user_id) COMMENT '帖子-用户唯一约束',
    INDEX idx_post_id (post_id) COMMENT '帖子ID索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- ============================================
-- 2. 在 bbs_post 表中添加 favorite_count 列
-- ============================================
ALTER TABLE bbs_pobbs_postbbs_postst 
ADD COLUMN favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数量'
AFTER view_count;

-- ============================================
-- 3. （可选）如果有现有数据，可以初始化收藏数量
-- ============================================
-- 注意：这一步只有在你需要根据现有收藏记录更新计数时才需要执行
-- UPDATE bbs_post p 
-- SET favorite_count = (
--     SELECT COUNT(*) FROM bbs_favorite f WHERE f.post_id = p.id
-- );
