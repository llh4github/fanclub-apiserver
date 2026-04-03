/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

-- 创建主播每日直播时长统计表
CREATE TABLE IF NOT EXISTS anchor_live_duration
(
    -- 数据主键
    id            BIGINT    NOT NULL,
    -- 直播间 ID
    room_id       BIGINT    NOT NULL,
    -- 直播时长 (秒)
    live_duration BIGINT    NOT NULL,
    -- 统计日期
    stat_date     DATE      NOT NULL,
    -- 创建时间，默认当前时间
    created_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 更新时间，默认当前时间
    updated_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 主键约束
    CONSTRAINT anchor_live_duration_pkey PRIMARY KEY (id),
    -- 唯一键约束 (room_id + stat_date)
    CONSTRAINT anchor_live_duration_live_stat_date_uk UNIQUE (room_id, stat_date),
    -- 检查约束：room_id 非负
    CONSTRAINT anchor_live_duration_room_id_check CHECK (room_id >= 0),
    -- 检查约束：live_duration 非负
    CONSTRAINT anchor_live_duration_live_duration_check CHECK (live_duration >= 0)
);

-- 添加表注释
COMMENT ON TABLE anchor_live_duration IS '主播每日直播时长统计';

-- 添加字段注释
COMMENT ON COLUMN anchor_live_duration.id IS '数据主键';
COMMENT ON COLUMN anchor_live_duration.room_id IS '直播间 ID';
COMMENT ON COLUMN anchor_live_duration.stat_date IS '统计日期';
COMMENT ON COLUMN anchor_live_duration.live_duration IS '直播时长 (秒)';
COMMENT ON COLUMN anchor_live_duration.created_time IS '创建时间';
COMMENT ON COLUMN anchor_live_duration.updated_time IS '更新时间';

-- 创建索引
CREATE INDEX IF NOT EXISTS anchor_live_duration_stat_date_idx ON anchor_live_duration (stat_date);

-- 创建触发器以自动更新 updated_time
CREATE OR REPLACE FUNCTION update_updated_time_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_anchor_live_duration_update_updated_time
    BEFORE UPDATE
    ON anchor_live_duration
    FOR EACH ROW
EXECUTE FUNCTION update_updated_time_column();
