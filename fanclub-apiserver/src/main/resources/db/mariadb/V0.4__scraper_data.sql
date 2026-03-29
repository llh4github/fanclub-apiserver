/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

alter table viewer_guard_buy_record
drop
column end_time;

alter table viewer_guard_buy_record
    add payflow_id varchar(30) not null comment '支付流水号';

alter table viewer_guard_buy_record
    add constraint viewer_guard_buy_record_payflow_id_uk
        unique (payflow_id);

alter table viewer_guard_buy_record
    change bid sender_bid bigint not null comment '赠送者BID';
alter table viewer_guard_buy_record
    add receiver_bid bigint unsigned not null comment '接受者BID';

alter table anchor_follower_num
    change created_at created_time datetime(3) default current_timestamp (3) not null comment '创建时间';

alter table anchor_follower_num
    change updated_at updated_time datetime(3) default current_timestamp (3) not null on
update current_timestamp(3) comment '更新时间';

alter table anchor_live_record
    change created_at created_time datetime(3) default current_timestamp (3) not null comment '创建时间';

alter table anchor_live_record
    change updated_at updated_time datetime(3) default current_timestamp (3) not null on
update current_timestamp(3) comment '更新时间';

----

CREATE TABLE `viewer_danmu_count`
(
    `id`           bigint unsigned NOT NULL COMMENT '主键ID',
    `bid`          bigint unsigned NOT NULL COMMENT 'B站UID',
    `cnt_date`     date NOT NULL COMMENT '统计时间',
    `cnt`          int  NOT NULL COMMENT '弹幕数量',
    `created_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bid_date` (`bid`, `cnt_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观众弹幕数量统计';
