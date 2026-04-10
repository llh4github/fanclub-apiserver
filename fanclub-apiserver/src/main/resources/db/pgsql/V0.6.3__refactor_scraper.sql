/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

alter table sys_scraper_cookie
    add uid bigint not null;

comment on column sys_scraper_cookie.uid is 'cookie值所属用户';
drop index sys_scraper_cookie_idx_cache_name;
drop index sys_scraper_cookie_sys_scraper_cookie_unique;

alter table sys_scraper_cookie
    add constraint sys_scraper_cookie_uk
        unique (uid, name, domain);

alter table viewer_danmu_count
    rename column rbid to room_id;
comment on column viewer_danmu_count.room_id is '直播间号';

alter table viewer_guard_buy_record
    rename column receiver_bid to room_id;
comment on column viewer_guard_buy_record.room_id is '直播间号';

