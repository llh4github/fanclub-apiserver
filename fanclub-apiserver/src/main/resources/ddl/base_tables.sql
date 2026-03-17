-- 爬虫功能配置表
CREATE TABLE `sys_scraper_feature`
(
    `id`           bigint unsigned NOT NULL COMMENT '数据主键',
    `follower`     tinyint(1) NOT NULL COMMENT '是否获取粉丝数',
    `anchor_id`    bigint unsigned NOT NULL COMMENT '主播ID',
    `created_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_anchor_id` (`anchor_id`),
    CONSTRAINT `fk_scraper_feature_anchor` FOREIGN KEY (`anchor_id`) REFERENCES `sys_anchor_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫功能配置';
-- 观众的舰长购买记录
CREATE TABLE `viewer_guard_buy_record`
(
    `id`           bigint unsigned NOT NULL COMMENT '数据主键',
    `bid`          bigint  NOT NULL COMMENT 'B站用户ID',
    `num`          tinyint NOT NULL COMMENT '购买数量',
    `guard_type`   tinyint NOT NULL COMMENT '舰长等级',
    `price`        int unsigned NOT NULL COMMENT '舰长价格',
    `start_time`   DATETIME(3) NULL COMMENT '舰长开始时间',
    `end_time`     DATETIME(3) NULL COMMENT '舰长结束时间',
    `created_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `viewer_guard_buy_record_bid_IDX` (`bid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观众的舰长购买记录';
-- 观众的基础信息表
CREATE TABLE `viewer_basic_info`
(
    `id`           bigint unsigned NOT NULL COMMENT '数据主键',
    `bid`          bigint       NOT NULL COMMENT 'B站用户ID',
    `nickname`     varchar(255) NOT NULL COMMENT '观众的B站昵称',
    `created_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bid_nkname` (`bid`, `nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观众的基础信息';
