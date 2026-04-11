/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

-- 主播直播日程安排表
CREATE TABLE anchor_live_schedule (
    id BIGINT NOT NULL,
    bid BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    start_time TIMESTAMP(3) NOT NULL,
    end_time TIMESTAMP(3) NOT NULL,
    created_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_anchor_live_schedule_bid CHECK (bid >= 0)
);

-- 表注释
COMMENT ON TABLE anchor_live_schedule IS '主播直播日程安排';
COMMENT ON COLUMN anchor_live_schedule.id IS '主键ID';
COMMENT ON COLUMN anchor_live_schedule.bid IS 'B站ID，通常称为UID';
COMMENT ON COLUMN anchor_live_schedule.topic IS '直播主题';
COMMENT ON COLUMN anchor_live_schedule.start_time IS '直播开始时间';
COMMENT ON COLUMN anchor_live_schedule.end_time IS '直播结束时间';
COMMENT ON COLUMN anchor_live_schedule.created_time IS '创建时间';
COMMENT ON COLUMN anchor_live_schedule.updated_time IS '更新时间';

-- 创建索引（加上表名前缀避免冲突）
CREATE INDEX idx_anchor_live_schedule_idx ON anchor_live_schedule(bid,topic,start_time,end_time);

-- 创建触发器：自动更新 updated_time 字段
CREATE OR REPLACE FUNCTION update_anchor_live_schedule_updated_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_anchor_live_schedule_updated_time
    BEFORE UPDATE ON anchor_live_schedule
    FOR EACH ROW
    EXECUTE FUNCTION update_anchor_live_schedule_updated_time();
