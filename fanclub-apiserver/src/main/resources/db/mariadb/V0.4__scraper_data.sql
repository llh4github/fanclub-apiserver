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
