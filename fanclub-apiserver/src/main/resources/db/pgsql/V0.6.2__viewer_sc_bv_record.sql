-- 创建观众SC点播的BV号记录表
CREATE TABLE viewer_sc_bv_record (
    id BIGINT NOT NULL,
    sc_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bv VARCHAR(12) NOT NULL,
    bid BIGINT NOT NULL,
    send_time TIMESTAMP NOT NULL,
    created_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_viewer_sc_bv_record PRIMARY KEY (id),
    CONSTRAINT uk_viewer_sc_bv_record_sc_id UNIQUE (sc_id),
    CONSTRAINT chk_viewer_sc_bv_record_room_id CHECK (room_id >= 0),
    CONSTRAINT chk_viewer_sc_bv_record_bid CHECK (bid >= 0)
);

-- 添加表注释
COMMENT ON TABLE viewer_sc_bv_record IS '观众SC点播的BV号记录';

-- 添加字段注释
COMMENT ON COLUMN viewer_sc_bv_record.id IS '数据主键';
COMMENT ON COLUMN viewer_sc_bv_record.sc_id IS 'SC的ID';
COMMENT ON COLUMN viewer_sc_bv_record.room_id IS '房间ID';
COMMENT ON COLUMN viewer_sc_bv_record.bv IS '发送的BV号';
COMMENT ON COLUMN viewer_sc_bv_record.bid IS '发送者的BID';
COMMENT ON COLUMN viewer_sc_bv_record.send_time IS '发送时间';
COMMENT ON COLUMN viewer_sc_bv_record.created_time IS '创建时间';
COMMENT ON COLUMN viewer_sc_bv_record.updated_time IS '更新时间';

/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

-- 创建索引
CREATE INDEX idx_viewer_sc_bv_record_room_bv ON viewer_sc_bv_record (room_id, bv);


-- 创建自动更新触发器函数
CREATE OR REPLACE FUNCTION update_updated_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER trg_viewer_sc_bv_record_updated_time
    BEFORE UPDATE ON viewer_sc_bv_record
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_time_column();
