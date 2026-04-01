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
