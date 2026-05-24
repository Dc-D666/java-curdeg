-- 为举报流程补充个人主页资料卡举报支持
-- 1. 给 bbs_report 增加 target_snapshot 字段
-- 2. 运行时约定：target_type=3 表示个人主页资料卡，handle_type=3 表示清空违规资料

ALTER TABLE bbs_report
    ADD COLUMN IF NOT EXISTS target_snapshot TEXT NULL COMMENT '举报对象快照（JSON）' AFTER target_id;
