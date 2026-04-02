create table viewer_danmu_count
(
    id           bigint unsigned                          not null comment '主键ID'
        primary key,
    bid          bigint unsigned                          not null comment 'B站UID',
    rbid         bigint unsigned                          not null comment '接收者的B站UID',
    cnt_date     date not null comment '统计时间',
    cnt          int  not null comment '弹幕数量',
    created_time datetime(3) default current_timestamp(3) not null comment '创建时间',
    updated_time datetime(3) default current_timestamp(3) not null on update current_timestamp(3) comment '更新时间',
    constraint uk_bid_date
        unique (bid, rbid, cnt_date)
) comment '观众弹幕数量统计';

alter table anchor_live_record
    add live_duration int unsigned null comment '直播时长(秒)' after end_live_time;


alter table anchor_live_record
drop
key uk_live_key;

alter table anchor_live_record
    add constraint room_live_record_uk
        unique (room_id, live_key);

create index anchor_live_record_live_time_index
    on anchor_live_record (live_time desc);

CREATE TABLE `anchor_live_duration`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '数据主键',
    `room_id`       BIGINT UNSIGNED NOT NULL COMMENT '直播间 ID',
    `stat_date`     DATE NOT NULL COMMENT '统计日期',
    `live_duration` int UNSIGNED NOT NULL COMMENT '直播时长 (秒)',
    `created_time`  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '数据创建时间',
    `updated_time`  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '数据更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `live_stat_date_uk` (`room_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主播每日直播时长统计';
