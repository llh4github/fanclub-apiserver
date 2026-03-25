-- fanclub.anchor_follower_num definition

CREATE TABLE `anchor_follower_num`
(
    `id`           bigint(20) unsigned NOT NULL COMMENT '主键ID',
    `follower_num` int(11)             NOT NULL COMMENT '粉丝数',
    `cnt_date`     date                NOT NULL COMMENT '统计日期',
    `bili_id`      bigint(20) unsigned NOT NULL COMMENT 'B站UID',
    `created_at`   datetime(3)         NOT NULL DEFAULT current_timestamp(3) COMMENT '创建时间',
    `updated_at`   datetime(3)         NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uid_cnt_date_uniq` (`bili_id`, `cnt_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='主播粉丝数';


-- fanclub.anchor_info definition

CREATE TABLE `anchor_info`
(
    `id`         bigint(20) unsigned NOT NULL COMMENT '主键ID',
    `bili_id`    bigint(20)          NOT NULL COMMENT 'B站ID，通常称为UID',
    `bili_name`  varchar(255)        NOT NULL COMMENT 'B站昵称',
    `room_id`    bigint(20)          NOT NULL COMMENT '直播间ID',
    `user_id`    bigint(20) unsigned NOT NULL COMMENT '用户ID',
    `created_at` datetime(3)         NOT NULL DEFAULT current_timestamp(3) COMMENT '创建时间',
    `updated_at` datetime(3)         NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3) COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='主播基础信息';


-- fanclub.sys_scraper_cookie definition

CREATE TABLE `sys_scraper_cookie`
(
    `id`           bigint(20)   NOT NULL COMMENT '主键 ID',
    `name`         varchar(50)  NOT NULL COMMENT 'Cookie 名称',
    `value`        varchar(255) NOT NULL COMMENT 'Cookie 值',
    `domain`       varchar(100) NOT NULL COMMENT 'Cookie 所属域名',
    `expires_at`   bigint(20)            DEFAULT NULL COMMENT 'Cookie 过期时间戳 (毫秒)',
    `created_time` datetime     NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    `updated_time` datetime     NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `sys_scraper_cookie_unique` (`domain`, `name`),
    KEY `idx_cache_name` (`domain`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='爬虫 Cookie 配置表';


-- fanclub.sys_user definition

CREATE TABLE `sys_user`
(
    `id`            bigint(20) unsigned NOT NULL COMMENT '主键',
    `username`      varchar(60)         NOT NULL COMMENT '用户名',
    `nickname`      varchar(60)         NOT NULL DEFAULT '' COMMENT '昵称',
    `password`      varchar(60)         NOT NULL COMMENT '密码',
    `created_time`  datetime            NOT NULL DEFAULT current_timestamp() COMMENT '数据创建时间',
    `updated_time`  datetime            NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '数据更新时间',
    `created_by_id` bigint(20) unsigned          DEFAULT NULL COMMENT '创建者ID',
    `updated_by_id` bigint(20) unsigned          DEFAULT NULL COMMENT '更新者ID',
    `role`          varchar(10)         NOT NULL COMMENT '用户所属角色',
    `anchor_id`     bigint(20) unsigned          DEFAULT NULL COMMENT '关联主播信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `sys_user_username_uniq` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统用户表';


-- fanclub.viewer_basic_info definition

CREATE TABLE `viewer_basic_info`
(
    `id`           bigint(20) unsigned NOT NULL COMMENT '数据主键',
    `bid`          bigint(20) unsigned NOT NULL COMMENT 'B站用户ID',
    `nickname`     varchar(255)        NOT NULL COMMENT '观众的B站昵称',
    `created_time` datetime(3) DEFAULT current_timestamp(3) COMMENT '创建时间',
    `updated_time` datetime(3) DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bid` (`bid`),
    KEY `bid_nickname_IDX` (`bid`, `nickname`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='观众的基础信息';


-- fanclub.viewer_guard_buy_record definition

CREATE TABLE `viewer_guard_buy_record`
(
    `id`           bigint(20) unsigned NOT NULL COMMENT '数据主键',
    `bid`          bigint(20)          NOT NULL COMMENT 'B站用户ID',
    `num`          tinyint(4)          NOT NULL COMMENT '购买数量',
    `guard_type`   tinyint(4)          NOT NULL COMMENT '舰长等级',
    `price`        int(11) unsigned    NOT NULL COMMENT '舰长价格',
    `start_time`   datetime(3) DEFAULT NULL COMMENT '舰长开始时间',
    `end_time`     datetime(3) DEFAULT NULL COMMENT '舰长结束时间',
    `created_time` datetime(3) DEFAULT current_timestamp(3) COMMENT '创建时间',
    `updated_time` datetime(3) DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `viewer_guard_buy_record_bid_IDX` (`bid`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='观众的舰长购买记录';


-- fanclub.sys_scraper_feature definition

CREATE TABLE `sys_scraper_feature`
(
    `id`           bigint(20) unsigned NOT NULL COMMENT '数据主键',
    `follower`     tinyint(1)          NOT NULL DEFAULT 1 COMMENT '是否获取粉丝数',
    `anchor_id`    bigint(20) unsigned NOT NULL COMMENT '主播ID',
    `created_time` datetime(3)                  DEFAULT current_timestamp(3) COMMENT '创建时间',
    `updated_time` datetime(3)                  DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3) COMMENT '更新时间',
    `monitor`      tinyint(1)          NOT NULL DEFAULT 1 COMMENT '是否数据监控',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_anchor_id` (`anchor_id`),
    CONSTRAINT `fk_scraper_feature_anchor` FOREIGN KEY (`anchor_id`) REFERENCES `anchor_info` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='爬虫功能配置';
