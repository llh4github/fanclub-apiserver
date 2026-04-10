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
