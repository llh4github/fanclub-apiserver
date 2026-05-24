-- anchor_song 表新增审计字段
ALTER TABLE anchor_song
    ADD COLUMN IF NOT EXISTS creator_id bigint,
    ADD COLUMN IF NOT EXISTS modifier_id bigint,
    ADD CONSTRAINT fk_anchor_song_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id),
    ADD CONSTRAINT fk_anchor_song_modifier FOREIGN KEY (modifier_id) REFERENCES sys_user(id);

-- 如果数据库已有 ip_hash 列，执行删除
ALTER TABLE treehole_submissions DROP COLUMN IF EXISTS ip_hash;

-- treehole_submissions 表新增字段
-- 1. B站ID（通常称为 UID）
ALTER TABLE treehole_submissions
    ADD COLUMN IF NOT EXISTS bid bigint NOT NULL;

-- 2. 修改者ID
ALTER TABLE treehole_submissions
    ADD COLUMN IF NOT EXISTS modifier_id bigint,
    ADD CONSTRAINT fk_treehole_submissions_modifier FOREIGN KEY (modifier_id) REFERENCES sys_user(id);

-- 3. 创建联合索引：topic_id + submit_time + bid（用于查询优化）
CREATE INDEX IF NOT EXISTS idx_submissions_query
    ON treehole_submissions (topic_id, submit_time, bid);

-- 4. 图片链接（PostgreSQL 原生数组，默认空数组）
ALTER TABLE treehole_submissions
    ADD COLUMN IF NOT EXISTS image_urls text[] DEFAULT '{}' NOT NULL;

-- 5. 图片链接是否已与 OSS 确认
ALTER TABLE treehole_submissions
    ADD COLUMN IF NOT EXISTS image_urls_confirmed boolean NOT NULL DEFAULT false;
-- treehole_topics 表新增审计字段
-- 1. 创建者ID
ALTER TABLE treehole_topics
    ADD COLUMN IF NOT EXISTS creator_id bigint,
    ADD CONSTRAINT fk_treehole_topics_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id);

-- 2. 修改者ID
ALTER TABLE treehole_topics
    ADD COLUMN IF NOT EXISTS modifier_id bigint,
    ADD CONSTRAINT fk_treehole_topics_modifier FOREIGN KEY (modifier_id) REFERENCES sys_user(id);