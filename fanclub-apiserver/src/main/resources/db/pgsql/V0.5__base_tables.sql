/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

-- 创建自动更新时间戳的触发器函数
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 主播粉丝数表
CREATE TABLE IF NOT EXISTS anchor_follower_num
(
    id           BIGINT PRIMARY KEY, -- 主键 ID
    follower_num INTEGER NOT NULL CHECK (follower_num >= 0), -- 粉丝数
    cnt_date     DATE NOT NULL, -- 统计日期
    bili_id      BIGINT NOT NULL CHECK (bili_id >= 0), -- B 站 UID
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 创建时间
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL -- 更新时间
);

COMMENT ON TABLE anchor_follower_num IS '主播粉丝数';
COMMENT ON COLUMN anchor_follower_num.id IS '主键 ID';
COMMENT ON COLUMN anchor_follower_num.follower_num IS '粉丝数';
COMMENT ON COLUMN anchor_follower_num.cnt_date IS '统计日期';
COMMENT ON COLUMN anchor_follower_num.bili_id IS 'B 站 UID';
COMMENT ON COLUMN anchor_follower_num.created_time IS '创建时间';
COMMENT ON COLUMN anchor_follower_num.updated_time IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS anchor_follower_num_uid_cnt_date_uniq ON anchor_follower_num (bili_id, cnt_date);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_anchor_follower_num_update_updated_time
    BEFORE UPDATE ON anchor_follower_num
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 主播基础信息表
CREATE TABLE IF NOT EXISTS anchor_info
(
    id         BIGINT PRIMARY KEY, -- 主键 ID
    bili_id    BIGINT NOT NULL, -- B 站 ID，通常称为 UID
    bili_name  VARCHAR(255) NOT NULL, -- B 站昵称
    room_id    BIGINT NOT NULL CHECK (room_id >= 0), -- 直播间 ID
    user_id    BIGINT CHECK (user_id >= 0), -- 用户 ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL -- 更新时间
);

COMMENT ON TABLE anchor_info IS '主播基础信息';
COMMENT ON COLUMN anchor_info.id IS '主键 ID';
COMMENT ON COLUMN anchor_info.bili_id IS 'B 站 ID，通常称为 UID';
COMMENT ON COLUMN anchor_info.bili_name IS 'B 站昵称';
COMMENT ON COLUMN anchor_info.room_id IS '直播间 ID';
COMMENT ON COLUMN anchor_info.user_id IS '用户 ID';
COMMENT ON COLUMN anchor_info.created_at IS '创建时间';
COMMENT ON COLUMN anchor_info.updated_at IS '更新时间';

-- 主播直播记录表
CREATE TABLE IF NOT EXISTS anchor_live_record
(
    id            BIGINT PRIMARY KEY, -- 主键
    room_id       BIGINT NOT NULL CHECK (room_id >= 0), -- 直播间 ID
    live_key      VARCHAR(255) NOT NULL, -- 直播场次 key
    live_time     TIMESTAMP NOT NULL, -- 直播开始时间
    live_status   SMALLINT NOT NULL CHECK (live_status >= 0), -- 直播状态
    end_live_time TIMESTAMP, -- 直播结束时间
    live_duration INTEGER CHECK (live_duration >= 0), -- 直播时长 (秒)
    created_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 创建时间
    updated_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL -- 更新时间
);

COMMENT ON TABLE anchor_live_record IS '主播直播记录';
COMMENT ON COLUMN anchor_live_record.id IS '主键';
COMMENT ON COLUMN anchor_live_record.room_id IS '直播间 ID';
COMMENT ON COLUMN anchor_live_record.live_key IS '直播场次 key';
COMMENT ON COLUMN anchor_live_record.live_time IS '直播开始时间';
COMMENT ON COLUMN anchor_live_record.live_status IS '直播状态';
COMMENT ON COLUMN anchor_live_record.end_live_time IS '直播结束时间';
COMMENT ON COLUMN anchor_live_record.live_duration IS '直播时长 (秒)';
COMMENT ON COLUMN anchor_live_record.created_time IS '创建时间';
COMMENT ON COLUMN anchor_live_record.updated_time IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS anchor_live_record_room_live_record_uk ON anchor_live_record (room_id, live_key);
CREATE INDEX IF NOT EXISTS anchor_live_record_anchor_live_record_live_time_index ON anchor_live_record (live_time DESC);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_anchor_live_record_update_updated_time
    BEFORE UPDATE ON anchor_live_record
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 爬虫 Cookie 配置表
CREATE TABLE IF NOT EXISTS sys_scraper_cookie
(
    id           BIGINT PRIMARY KEY, -- 主键 ID
    name         VARCHAR(50) NOT NULL, -- Cookie 名称
    value        VARCHAR(255) NOT NULL, -- Cookie 值
    domain       VARCHAR(100) NOT NULL, -- Cookie 所属域名
    expires_at   BIGINT CHECK (expires_at >= 0), -- Cookie 过期时间戳 (毫秒)
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 创建时间
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL -- 更新时间
);

COMMENT ON TABLE sys_scraper_cookie IS '爬虫 Cookie 配置表';
COMMENT ON COLUMN sys_scraper_cookie.id IS '主键 ID';
COMMENT ON COLUMN sys_scraper_cookie.name IS 'Cookie 名称';
COMMENT ON COLUMN sys_scraper_cookie.value IS 'Cookie 值';
COMMENT ON COLUMN sys_scraper_cookie.domain IS 'Cookie 所属域名';
COMMENT ON COLUMN sys_scraper_cookie.expires_at IS 'Cookie 过期时间戳 (毫秒)';
COMMENT ON COLUMN sys_scraper_cookie.created_time IS '创建时间';
COMMENT ON COLUMN sys_scraper_cookie.updated_time IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS sys_scraper_cookie_sys_scraper_cookie_unique ON sys_scraper_cookie (domain, name);
CREATE INDEX IF NOT EXISTS sys_scraper_cookie_idx_cache_name ON sys_scraper_cookie (domain, name);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_sys_scraper_cookie_update_updated_time
    BEFORE UPDATE ON sys_scraper_cookie
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 爬虫功能配置表
CREATE TABLE IF NOT EXISTS sys_scraper_feature
(
    id           BIGINT PRIMARY KEY, -- 数据主键
    follower     BOOLEAN DEFAULT TRUE NOT NULL, -- 是否获取粉丝数
    anchor_id    BIGINT NOT NULL CHECK (anchor_id >= 0), -- 主播 ID
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    monitor      BOOLEAN DEFAULT TRUE NOT NULL -- 是否数据监控
);

COMMENT ON TABLE sys_scraper_feature IS '爬虫功能配置';
COMMENT ON COLUMN sys_scraper_feature.id IS '数据主键';
COMMENT ON COLUMN sys_scraper_feature.follower IS '是否获取粉丝数';
COMMENT ON COLUMN sys_scraper_feature.anchor_id IS '主播 ID';
COMMENT ON COLUMN sys_scraper_feature.created_time IS '创建时间';
COMMENT ON COLUMN sys_scraper_feature.updated_time IS '更新时间';
COMMENT ON COLUMN sys_scraper_feature.monitor IS '是否数据监控';

CREATE UNIQUE INDEX IF NOT EXISTS sys_scraper_feature_uk_anchor_id ON sys_scraper_feature (anchor_id);

ALTER TABLE sys_scraper_feature
    ADD CONSTRAINT fk_scraper_feature_anchor
    FOREIGN KEY (anchor_id) REFERENCES anchor_info (id);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_sys_scraper_feature_update_updated_time
    BEFORE UPDATE ON sys_scraper_feature
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user
(
    id            BIGINT PRIMARY KEY, -- 主键
    username      VARCHAR(60) NOT NULL, -- 用户名
    nickname      VARCHAR(60) DEFAULT '' NOT NULL, -- 昵称
    password      VARCHAR(60) NOT NULL, -- 密码
    created_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 数据创建时间
    updated_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 数据更新时间
    created_by_id BIGINT CHECK (created_by_id >= 0), -- 创建者 ID
    updated_by_id BIGINT CHECK (updated_by_id >= 0), -- 更新者 ID
    role          VARCHAR(10) NOT NULL, -- 用户所属角色
    anchor_id     BIGINT CHECK (anchor_id >= 0) -- 关联主播信息
);

COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '主键';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.password IS '密码';
COMMENT ON COLUMN sys_user.created_time IS '数据创建时间';
COMMENT ON COLUMN sys_user.updated_time IS '数据更新时间';
COMMENT ON COLUMN sys_user.created_by_id IS '创建者 ID';
COMMENT ON COLUMN sys_user.updated_by_id IS '更新者 ID';
COMMENT ON COLUMN sys_user.role IS '用户所属角色';
COMMENT ON COLUMN sys_user.anchor_id IS '关联主播信息';

CREATE UNIQUE INDEX IF NOT EXISTS sys_user_sys_user_username_uniq ON sys_user (username);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_sys_user_update_updated_time
    BEFORE UPDATE ON sys_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 观众的基础信息表
CREATE TABLE IF NOT EXISTS viewer_basic_info
(
    id           BIGINT PRIMARY KEY, -- 数据主键
    bid          BIGINT NOT NULL CHECK (bid >= 0), -- B 站用户 ID
    nickname     VARCHAR(255) NOT NULL, -- 观众的 B 站昵称
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 更新时间
);

COMMENT ON TABLE viewer_basic_info IS '观众的基础信息';
COMMENT ON COLUMN viewer_basic_info.id IS '数据主键';
COMMENT ON COLUMN viewer_basic_info.bid IS 'B 站用户 ID';
COMMENT ON COLUMN viewer_basic_info.nickname IS '观众的 B 站昵称';
COMMENT ON COLUMN viewer_basic_info.created_time IS '创建时间';
COMMENT ON COLUMN viewer_basic_info.updated_time IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS viewer_basic_info_uk_bid_nkname ON viewer_basic_info (bid, nickname);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_viewer_basic_info_update_updated_time
    BEFORE UPDATE ON viewer_basic_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 观众的舰长购买记录表
CREATE TABLE IF NOT EXISTS viewer_guard_buy_record
(
    id           BIGINT PRIMARY KEY, -- 数据主键
    sender_bid   BIGINT NOT NULL CHECK (sender_bid >= 0), -- 赠送者 BID
    num          SMALLINT NOT NULL CHECK (num > 0), -- 购买数量
    guard_type   SMALLINT NOT NULL CHECK (guard_type > 0), -- 舰长等级
    price        INTEGER NOT NULL CHECK (price > 0), -- 舰长价格
    start_time   TIMESTAMP, -- 舰长开始时间
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    payflow_id   VARCHAR(30) NOT NULL, -- 支付流水号
    receiver_bid BIGINT NOT NULL CHECK (receiver_bid >= 0) -- 接受者 BID
);

COMMENT ON TABLE viewer_guard_buy_record IS '观众的舰长购买记录';
COMMENT ON COLUMN viewer_guard_buy_record.id IS '数据主键';
COMMENT ON COLUMN viewer_guard_buy_record.sender_bid IS '赠送者 BID';
COMMENT ON COLUMN viewer_guard_buy_record.num IS '购买数量';
COMMENT ON COLUMN viewer_guard_buy_record.guard_type IS '舰长等级';
COMMENT ON COLUMN viewer_guard_buy_record.price IS '舰长价格';
COMMENT ON COLUMN viewer_guard_buy_record.start_time IS '舰长开始时间';
COMMENT ON COLUMN viewer_guard_buy_record.created_time IS '创建时间';
COMMENT ON COLUMN viewer_guard_buy_record.updated_time IS '更新时间';
COMMENT ON COLUMN viewer_guard_buy_record.payflow_id IS '支付流水号';
COMMENT ON COLUMN viewer_guard_buy_record.receiver_bid IS '接受者 BID';

CREATE UNIQUE INDEX IF NOT EXISTS viewer_guard_buy_record_viewer_guard_buy_record_payflow_id_uk ON viewer_guard_buy_record (payflow_id);
CREATE INDEX IF NOT EXISTS viewer_guard_buy_record_viewer_guard_buy_record_bid_IDX ON viewer_guard_buy_record (sender_bid);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_viewer_guard_buy_record_update_updated_time
    BEFORE UPDATE ON viewer_guard_buy_record
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();

-- 观众弹幕数量统计表
CREATE TABLE IF NOT EXISTS viewer_danmu_count
(
    id           BIGINT PRIMARY KEY, -- 主键 ID
    bid          BIGINT NOT NULL CHECK (bid >= 0), -- B 站 UID
    rbid         BIGINT NOT NULL CHECK (rbid >= 0), -- 接收者的 B 站 UID
    cnt_date     DATE NOT NULL, -- 统计时间
    cnt          INTEGER NOT NULL CHECK (cnt >= 0), -- 弹幕数量
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 创建时间
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL -- 更新时间
);

COMMENT ON TABLE viewer_danmu_count IS '观众弹幕数量统计';
COMMENT ON COLUMN viewer_danmu_count.id IS '主键 ID';
COMMENT ON COLUMN viewer_danmu_count.bid IS 'B 站 UID';
COMMENT ON COLUMN viewer_danmu_count.rbid IS '接收者的 B 站 UID';
COMMENT ON COLUMN viewer_danmu_count.cnt_date IS '统计时间';
COMMENT ON COLUMN viewer_danmu_count.cnt IS '弹幕数量';
COMMENT ON COLUMN viewer_danmu_count.created_time IS '创建时间';
COMMENT ON COLUMN viewer_danmu_count.updated_time IS '更新时间';

create unique index viewer_danmu_count_uk_bid_date on viewer_danmu_count (cnt_date, rbid, bid);

-- 创建触发器以自动更新 updated_time
CREATE TRIGGER trigger_viewer_danmu_count_update_updated_time
    BEFORE UPDATE ON viewer_danmu_count
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();