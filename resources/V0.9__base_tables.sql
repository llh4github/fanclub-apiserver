-- PostgreSQL Database Schema DDL
-- Generated based on Go model definitions
-- Version: 1.0
-- Generated at: 2026-05-07

-- ============================================================================
-- 说明：
-- - 主键ID由应用程序使用 snowflake 算法生成，GORM BeforeCreate hook 自动赋值
-- - 所有表使用统一的 updated_time 触发器自动更新
-- - 按依赖顺序创建表：先创建被引用的表，再创建引用它的表
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 创建触发器函数 (所有表共用)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 表1: sys_user (系统用户表)
-- 说明: 存储系统用户信息，包括用户名、密码、角色等
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    -- 主键，使用 snowflake 算法生成 (由应用程序赋值)
    id BIGINT PRIMARY KEY,

    -- 创建时间 (毫秒级)
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间 (毫秒级)
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 用户名，用于登录 (唯一)
    username VARCHAR(60) NOT NULL UNIQUE,

    -- 昵称
    nickname VARCHAR(60) NOT NULL DEFAULT '',

    -- 密码 (bcrypt加密后存储)
    password VARCHAR(60) NOT NULL,

    -- 创建者ID (外键引用sys_user.id，可为空表示系统创建)
    created_by_id BIGINT REFERENCES sys_user(id),

    -- 更新者ID (外键引用sys_user.id)
    updated_by_id BIGINT REFERENCES sys_user(id),

    -- 用户角色 (ADMIN/ANCHOR/GUEST)
    role VARCHAR(10) NOT NULL,

    -- 最后登录时间 (毫秒级)
    last_login_time TIMESTAMPTZ
);

-- 创建触发器自动更新 updated_time
CREATE OR REPLACE TRIGGER update_sys_user_updated_time
    BEFORE UPDATE ON sys_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_sys_user_created_by_id ON sys_user(created_by_id) WHERE created_by_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sys_user_updated_by_id ON sys_user(updated_by_id) WHERE updated_by_id IS NOT NULL;

COMMENT ON TABLE sys_user IS '系统用户表，存储用户登录信息和角色';
COMMENT ON COLUMN sys_user.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN sys_user.username IS '用户名，用于登录，必须唯一';
COMMENT ON COLUMN sys_user.nickname IS '用户昵称';
COMMENT ON COLUMN sys_user.password IS '密码，bcrypt加密后存储';
COMMENT ON COLUMN sys_user.created_by_id IS '创建者ID，引用sys_user表';
COMMENT ON COLUMN sys_user.updated_by_id IS '更新者ID，引用sys_user表';
COMMENT ON COLUMN sys_user.role IS '用户角色：ADMIN(管理员)、ANCHOR(主播)、GUEST(访客)';
COMMENT ON COLUMN sys_user.last_login_time IS '最后登录时间，毫秒级精度，由应用程序在登录时自动更新';
COMMENT ON COLUMN sys_user.created_time IS '记录创建时间，毫秒级精度';
COMMENT ON COLUMN sys_user.updated_time IS '记录更新时间，毫秒级精度';

-- ----------------------------------------------------------------------------
-- 表2: anchor_info (主播基础信息表)
-- 说明: 存储主播的B站信息，包括UID、昵称、直播间ID等
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS anchor_info (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- B站UID (必填)
    bid BIGINT NOT NULL,

    -- B站昵称 (必填)
    bili_name VARCHAR(255) NOT NULL,

    -- 直播间ID (必填)
    room_id BIGINT NOT NULL,

    -- 关联的系统用户ID (外键，可为空表示未绑定用户)
    user_id BIGINT REFERENCES sys_user(id)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_anchor_info_updated_time
    BEFORE UPDATE ON anchor_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_anchor_info_bid ON anchor_info(bid);
CREATE INDEX IF NOT EXISTS idx_anchor_info_room_id ON anchor_info(room_id);
CREATE INDEX IF NOT EXISTS idx_anchor_info_user_id ON anchor_info(user_id) WHERE user_id IS NOT NULL;

COMMENT ON TABLE anchor_info IS '主播基础信息表，存储B站主播的UID、昵称、直播间等信息';
COMMENT ON COLUMN anchor_info.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN anchor_info.bid IS 'B站UID，用于标识主播身份';
COMMENT ON COLUMN anchor_info.bili_name IS 'B站昵称，显示用';
COMMENT ON COLUMN anchor_info.room_id IS 'B站直播间ID，用于获取直播信息';
COMMENT ON COLUMN anchor_info.user_id IS '关联的系统用户ID，外键引用sys_user表，可为空表示未绑定账户';
COMMENT ON COLUMN anchor_info.created_time IS '记录创建时间';
COMMENT ON COLUMN anchor_info.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表3: anchor_follower_num (主播粉丝数记录表)
-- 说明: 记录主播每日的粉丝数量，用于数据分析
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS anchor_follower_num (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 粉丝数
    follower_num INT NOT NULL,

    -- 统计日期
    cnt_date DATE NOT NULL,

    -- B站UID
    bid BIGINT NOT NULL,

    -- 约束：粉丝数非负
    CONSTRAINT chk_follower_num_non_negative CHECK (follower_num >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_anchor_follower_num_updated_time
    BEFORE UPDATE ON anchor_follower_num
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建复合唯一索引 (bid + cnt_date)
CREATE UNIQUE INDEX IF NOT EXISTS idx_bid_cntdate ON anchor_follower_num(bid, cnt_date);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_anchor_follower_num_bid ON anchor_follower_num(bid);
CREATE INDEX IF NOT EXISTS idx_anchor_follower_num_cnt_date ON anchor_follower_num(cnt_date);

COMMENT ON TABLE anchor_follower_num IS '主播粉丝数记录表，记录每日粉丝数量变化';
COMMENT ON COLUMN anchor_follower_num.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN anchor_follower_num.follower_num IS '当日粉丝数量';
COMMENT ON COLUMN anchor_follower_num.cnt_date IS '统计日期';
COMMENT ON COLUMN anchor_follower_num.bid IS 'B站UID';
COMMENT ON COLUMN anchor_follower_num.created_time IS '记录创建时间';
COMMENT ON COLUMN anchor_follower_num.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表4: anchor_song (主播歌曲表)
-- 说明: 存储主播的点歌列表，包括歌曲名称、价格等
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS anchor_song (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 歌曲价格(元)，默认0
    price INT DEFAULT 0,

    -- 主播B站ID
    bid BIGINT NOT NULL,

    -- 歌曲名称
    name VARCHAR(255) NOT NULL,

    -- BV号
    bv VARCHAR(15) DEFAULT '',

    -- 约束：价格非负
    CONSTRAINT chk_price_non_negative CHECK (price >= 0),
    CONSTRAINT chk_bid_non_negative CHECK (bid >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_anchor_song_updated_time
    BEFORE UPDATE ON anchor_song
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建复合唯一索引 (bid + name)
CREATE UNIQUE INDEX IF NOT EXISTS anchor_song_bid_name_uindex ON anchor_song(bid, name);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_anchor_song_bid ON anchor_song(bid);

COMMENT ON TABLE anchor_song IS '主播歌曲表，存储主播的点歌列表';
COMMENT ON COLUMN anchor_song.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN anchor_song.price IS '歌曲价格，单位元，默认0表示免费';
COMMENT ON COLUMN anchor_song.bid IS '主播B站UID';
COMMENT ON COLUMN anchor_song.name IS '歌曲名称';
COMMENT ON COLUMN anchor_song.bv IS 'B站视频号，用于标识歌曲来源';
COMMENT ON COLUMN anchor_song.created_time IS '记录创建时间';
COMMENT ON COLUMN anchor_song.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表5: anchor_live_record (主播直播记录表)
-- 说明: 记录主播每次直播的信息，包括开播时间、结束时间、时长等
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS anchor_live_record (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 直播间ID (必填)
    room_id BIGINT NOT NULL,

    -- 直播场次key (必填，用于唯一标识每次直播)
    live_key VARCHAR(255) NOT NULL,

    -- 直播开始时间 (必填)
    live_time TIMESTAMPTZ NOT NULL,

    -- 直播状态 (0=未开始, 1=直播中, 2=已结束)
    live_status SMALLINT NOT NULL,

    -- 直播结束时间
    end_live_time TIMESTAMPTZ,

    -- 直播时长(秒)
    live_duration INT,

    -- 约束
    CONSTRAINT chk_room_id_non_negative CHECK (room_id >= 0),
    CONSTRAINT chk_live_status_non_negative CHECK (live_status >= 0),
    CONSTRAINT chk_live_duration_non_negative CHECK (live_duration IS NULL OR live_duration >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_anchor_live_record_updated_time
    BEFORE UPDATE ON anchor_live_record
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建复合唯一索引 (room_id + live_key)
CREATE UNIQUE INDEX IF NOT EXISTS anchor_live_record_room_live_record_uk ON anchor_live_record(room_id, live_key);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_anchor_live_record_room_id ON anchor_live_record(room_id);
CREATE INDEX IF NOT EXISTS idx_anchor_live_record_live_time ON anchor_live_record(live_time DESC);

COMMENT ON TABLE anchor_live_record IS '主播直播记录表，记录每次直播的开播、结束时间等信息';
COMMENT ON COLUMN anchor_live_record.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN anchor_live_record.room_id IS 'B站直播间ID';
COMMENT ON COLUMN anchor_live_record.live_key IS '直播场次唯一标识Key';
COMMENT ON COLUMN anchor_live_record.live_time IS '直播开始时间';
COMMENT ON COLUMN anchor_live_record.live_status IS '直播状态：0=未开始, 1=直播中, 2=已结束';
COMMENT ON COLUMN anchor_live_record.end_live_time IS '直播结束时间';
COMMENT ON COLUMN anchor_live_record.live_duration IS '直播时长，单位秒';
COMMENT ON COLUMN anchor_live_record.created_time IS '记录创建时间';
COMMENT ON COLUMN anchor_live_record.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表6: anchor_live_schedule (主播直播日程表)
-- 说明: 存储主播的周直播日程安排
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS anchor_live_schedule (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- B站UID
    bid BIGINT NOT NULL,

    -- 直播主题
    topic VARCHAR(255) NOT NULL,

    -- 直播主题emoji
    emoji VARCHAR(10) NOT NULL DEFAULT '',

    -- 直播开始时间
    start_time TIMESTAMPTZ NOT NULL,

    -- 直播结束时间
    end_time TIMESTAMPTZ NOT NULL,

    -- 约束
    CONSTRAINT chk_bid_non_negative CHECK (bid >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_anchor_live_schedule_updated_time
    BEFORE UPDATE ON anchor_live_schedule
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建复合索引 (bid, topic, start_time, end_time)
CREATE INDEX IF NOT EXISTS idx_anchor_live_schedule_idx ON anchor_live_schedule(bid, topic, start_time, end_time);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_anchor_live_schedule_bid ON anchor_live_schedule(bid);
CREATE INDEX IF NOT EXISTS idx_anchor_live_schedule_start_time ON anchor_live_schedule(start_time);

COMMENT ON TABLE anchor_live_schedule IS '主播直播日程表，存储周直播计划安排';
COMMENT ON COLUMN anchor_live_schedule.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN anchor_live_schedule.bid IS 'B站UID';
COMMENT ON COLUMN anchor_live_schedule.topic IS '直播主题';
COMMENT ON COLUMN anchor_live_schedule.emoji IS '直播主题对应的emoji表情';
COMMENT ON COLUMN anchor_live_schedule.start_time IS '计划开播时间';
COMMENT ON COLUMN anchor_live_schedule.end_time IS '计划结束时间';
COMMENT ON COLUMN anchor_live_schedule.created_time IS '记录创建时间';
COMMENT ON COLUMN anchor_live_schedule.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表7: sys_scraper_cookie (爬虫Cookie配置表)
-- 说明: 存储爬虫使用的Cookie信息
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_scraper_cookie (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Cookie名称
    name VARCHAR(50) NOT NULL,

    -- Cookie值
    value VARCHAR(255) NOT NULL,

    -- Cookie所属域名
    domain VARCHAR(100) NOT NULL,

    -- Cookie过期时间戳(毫秒)
    expires_at BIGINT,

    -- Cookie所属用户UID
    uid BIGINT NOT NULL,

    -- 约束
    CONSTRAINT chk_expires_at_non_negative CHECK (expires_at IS NULL OR expires_at >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_sys_scraper_cookie_updated_time
    BEFORE UPDATE ON sys_scraper_cookie
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建复合唯一索引 (uid, name, domain)
CREATE UNIQUE INDEX IF NOT EXISTS uk_uid_name_domain ON sys_scraper_cookie(uid, name, domain);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_sys_scraper_cookie_uid ON sys_scraper_cookie(uid);
CREATE INDEX IF NOT EXISTS idx_sys_scraper_cookie_domain ON sys_scraper_cookie(domain);

COMMENT ON TABLE sys_scraper_cookie IS '爬虫Cookie配置表，存储用于B站爬虫的Cookie信息';
COMMENT ON COLUMN sys_scraper_cookie.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN sys_scraper_cookie.name IS 'Cookie名称';
COMMENT ON COLUMN sys_scraper_cookie.value IS 'Cookie的实际值';
COMMENT ON COLUMN sys_scraper_cookie.domain IS 'Cookie所属域名，如bilibili.com';
COMMENT ON COLUMN sys_scraper_cookie.expires_at IS 'Cookie过期时间戳，毫秒级，可为空表示永不过期';
COMMENT ON COLUMN sys_scraper_cookie.uid IS 'Cookie所属用户UID';
COMMENT ON COLUMN sys_scraper_cookie.created_time IS '记录创建时间';
COMMENT ON COLUMN sys_scraper_cookie.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表8: sys_scraper_feature (爬虫功能配置表)
-- 说明: 配置各主播的爬虫功能开关
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_scraper_feature (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 是否获取粉丝数
    follower BOOLEAN NOT NULL DEFAULT TRUE,

    -- 主播ID (外键引用anchor_info.id)
    anchor_id BIGINT NOT NULL,

    -- 是否启用数据监控
    monitor BOOLEAN NOT NULL DEFAULT TRUE,

    -- 约束
    CONSTRAINT chk_anchor_id_non_negative CHECK (anchor_id >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_sys_scraper_feature_updated_time
    BEFORE UPDATE ON sys_scraper_feature
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建唯一索引 (anchor_id)
CREATE UNIQUE INDEX IF NOT EXISTS uk_anchor_id ON sys_scraper_feature(anchor_id);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_sys_scraper_feature_monitor ON sys_scraper_feature(monitor) WHERE monitor = TRUE;

-- 添加外键约束 (在anchor_info表创建后添加)
ALTER TABLE sys_scraper_feature
    ADD CONSTRAINT fk_sys_scraper_feature_anchor_id
    FOREIGN KEY (anchor_id) REFERENCES anchor_info(id);

COMMENT ON TABLE sys_scraper_feature IS '爬虫功能配置表，配置各主播的爬虫功能开关';
COMMENT ON COLUMN sys_scraper_feature.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN sys_scraper_feature.follower IS '是否启用粉丝数爬取功能';
COMMENT ON COLUMN sys_scraper_feature.anchor_id IS '主播ID，外键引用anchor_info表';
COMMENT ON COLUMN sys_scraper_feature.monitor IS '是否启用实时数据监控';
COMMENT ON COLUMN sys_scraper_feature.created_time IS '记录创建时间';
COMMENT ON COLUMN sys_scraper_feature.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表9: viewer_sc_bv_record (观众SC点播BV号记录表)
-- 说明: 记录观众发送的SC点歌BV号信息
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS viewer_sc_bv_record (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- SC的ID (必填，唯一)
    sc_id BIGINT NOT NULL,

    -- 房间ID
    room_id BIGINT NOT NULL,

    -- 发送的BV号
    bv VARCHAR(12) NOT NULL,

    -- 发送者的BID
    bid BIGINT NOT NULL,

    -- 发送时间
    send_time TIMESTAMPTZ NOT NULL,

    -- 约束
    CONSTRAINT chk_room_id_non_negative CHECK (room_id >= 0),
    CONSTRAINT chk_bid_non_negative CHECK (bid >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_viewer_sc_bv_record_updated_time
    BEFORE UPDATE ON viewer_sc_bv_record
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建唯一索引 (sc_id)
CREATE UNIQUE INDEX IF NOT EXISTS uk_viewer_sc_bv_record_sc_id ON viewer_sc_bv_record(sc_id);

-- 创建复合索引 (room_id, bv)
CREATE INDEX IF NOT EXISTS idx_viewer_sc_bv_record_room_bv ON viewer_sc_bv_record(room_id, bv);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_viewer_sc_bv_record_room_id ON viewer_sc_bv_record(room_id);
CREATE INDEX IF NOT EXISTS idx_viewer_sc_bv_record_bid ON viewer_sc_bv_record(bid);
CREATE INDEX IF NOT EXISTS idx_viewer_sc_bv_record_send_time ON viewer_sc_bv_record(send_time DESC);

COMMENT ON TABLE viewer_sc_bv_record IS '观众SC点播BV号记录表，记录观众发送的SuperChat点歌信息';
COMMENT ON COLUMN viewer_sc_bv_record.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN viewer_sc_bv_record.sc_id IS 'SuperChat的唯一ID';
COMMENT ON COLUMN viewer_sc_bv_record.room_id IS 'B站直播间ID';
COMMENT ON COLUMN viewer_sc_bv_record.bv IS '点播的B站视频号';
COMMENT ON COLUMN viewer_sc_bv_record.bid IS '发送者的B站UID';
COMMENT ON COLUMN viewer_sc_bv_record.send_time IS '发送时间';
COMMENT ON COLUMN viewer_sc_bv_record.created_time IS '记录创建时间';
COMMENT ON COLUMN viewer_sc_bv_record.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表10: treehole_topics (树洞主题表)
-- 说明: 存储树洞功能的主题配置，包括投稿开放/关闭时间等
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS treehole_topics (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 主播B站UID
    bid BIGINT NOT NULL,

    -- 树洞主题标题
    title VARCHAR(125) NOT NULL,

    -- 树洞主题描述
    description VARCHAR(300),

    -- 投稿开放时间窗口
    open_at TIMESTAMPTZ NOT NULL,

    -- 投稿关闭时间窗口
    close_at TIMESTAMPTZ NOT NULL,

    -- 是否启用
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- 约束：关闭时间必须晚于开放时间
    CONSTRAINT chk_close_after_open CHECK (close_at > open_at),
    CONSTRAINT chk_bid_non_negative CHECK (bid >= 0)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_treehole_topics_updated_time
    BEFORE UPDATE ON treehole_topics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_treehole_topics_bid ON treehole_topics(bid);
CREATE INDEX IF NOT EXISTS idx_treehole_topics_is_active ON treehole_topics(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_treehole_topics_open_at ON treehole_topics(open_at);
CREATE INDEX IF NOT EXISTS idx_treehole_topics_close_at ON treehole_topics(close_at);

COMMENT ON TABLE treehole_topics IS '树洞主题表，配置树洞功能的投稿主题和开放时间';
COMMENT ON COLUMN treehole_topics.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN treehole_topics.bid IS '主播B站UID，标识该主题属于哪个主播';
COMMENT ON COLUMN treehole_topics.title IS '树洞主题标题';
COMMENT ON COLUMN treehole_topics.description IS '树洞主题描述，详细说明该主题的投稿要求';
COMMENT ON COLUMN treehole_topics.open_at IS '投稿开放时间，在此时间之前不能投稿';
COMMENT ON COLUMN treehole_topics.close_at IS '投稿关闭时间，在此时间之后不能投稿';
COMMENT ON COLUMN treehole_topics.is_active IS '是否启用该主题，FALSE表示暂停投稿';
COMMENT ON COLUMN treehole_topics.created_time IS '记录创建时间';
COMMENT ON COLUMN treehole_topics.updated_time IS '记录更新时间';

-- ----------------------------------------------------------------------------
-- 表11: treehole_submissions (树洞投稿记录表)
-- 说明: 存储用户提交的树洞投稿内容
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS treehole_submissions (
    -- 主键，使用 snowflake 算法生成
    id BIGINT PRIMARY KEY,

    -- 创建时间
    created_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 更新时间
    updated_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 稿件ID（由 sonyflake 生成，使用36进制编码）
    submission_id VARCHAR(50) NOT NULL UNIQUE,

    -- 关联的主题ID (外键)
    topic_id BIGINT NOT NULL,

    -- 原始投稿内容 (Markdown格式)
    content_markdown TEXT NOT NULL,

    -- 投稿内容 (Markdown转HTML后的内容)
    content_html TEXT NOT NULL,

    -- 投稿摘要
    summary TEXT NOT NULL,

    -- 投稿时间
    submit_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 审核状态: 0=不宜展示, 1=未审核, 2=可以展示
    audit_status SMALLINT NOT NULL DEFAULT 1,

    -- 约束
    CONSTRAINT fk_treehole_topic FOREIGN KEY (topic_id) REFERENCES treehole_topics(id) ON DELETE CASCADE,
    CONSTRAINT chk_audit_status_range CHECK (audit_status >= 0 AND audit_status <= 2)
);

-- 创建触发器
CREATE OR REPLACE TRIGGER update_treehole_submissions_updated_time
    BEFORE UPDATE ON treehole_submissions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_treehole_submissions_topic_submit_time ON treehole_submissions(topic_id, submit_time DESC);
CREATE INDEX IF NOT EXISTS idx_treehole_submissions_topic_id ON treehole_submissions(topic_id);
CREATE INDEX IF NOT EXISTS idx_treehole_submissions_audit_status ON treehole_submissions(audit_status) WHERE audit_status = 2;

COMMENT ON TABLE treehole_submissions IS '树洞投稿记录表，存储用户提交的树洞内容';
COMMENT ON COLUMN treehole_submissions.id IS '主键，使用snowflake算法生成，由应用程序赋值';
COMMENT ON COLUMN treehole_submissions.submission_id IS '稿件ID，由sonyflake生成，使用36进制编码，用于对外展示';
COMMENT ON COLUMN treehole_submissions.topic_id IS '关联的主题ID，外键引用treehole_topics表';
COMMENT ON COLUMN treehole_submissions.content_markdown IS '原始投稿内容，Markdown格式，用于后续编辑或重新渲染';
COMMENT ON COLUMN treehole_submissions.content_html IS '投稿内容，Markdown格式转换为HTML后的内容，用于前端展示';
COMMENT ON COLUMN treehole_submissions.summary IS '投稿摘要，用于列表展示';
COMMENT ON COLUMN treehole_submissions.submit_time IS '用户提交时间';
COMMENT ON COLUMN treehole_submissions.audit_status IS '审核状态：0=不宜展示, 1=未审核, 2=可以展示';
COMMENT ON COLUMN treehole_submissions.created_time IS '记录创建时间';
COMMENT ON COLUMN treehole_submissions.updated_time IS '记录更新时间';

-- ============================================================================
-- 结束
-- ============================================================================
