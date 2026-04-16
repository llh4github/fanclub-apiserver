/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

create table anchor_song
(
    id            bigint                                     not null
        constraint pk_anchor_song
            primary key,
    created_time  timestamp(3) default CURRENT_TIMESTAMP     not null,
    updated_time  timestamp(3) default CURRENT_TIMESTAMP     not null,
    created_by_id bigint
        constraint sys_user_created_by_id_check
            check (created_by_id >= 0),
    updated_by_id bigint
        constraint sys_user_updated_by_id_check
            check (updated_by_id >= 0),

    price         integer      default 0                     not null
        constraint anchor_song_price_check
            check (price >= 0),
    bid           bigint                                     not null
        constraint anchor_song_bid_check
            check (bid >= 0),
    name          varchar(255)                               not null,
    bv            varchar(15)  default ''::character varying not null
);

comment on table anchor_song is '主播歌曲';
comment on column anchor_song.id is '数据主键';
comment on column anchor_song.bid is '主播B站ID';
comment on column anchor_song.name is '歌曲名称';
comment on column anchor_song.price is '歌曲价格(元)';
comment on column anchor_song.created_time is '数据创建时间';
comment on column anchor_song.updated_time is '数据更新时间';
comment on column anchor_song.created_by_id is '创建者 ID';
comment on column anchor_song.updated_by_id is '更新者 ID';
comment on column anchor_song.bv is 'bv号';

alter table anchor_song
    owner to postgres;

create unique index anchor_song_bid_name_uindex
    on anchor_song (bid, name);