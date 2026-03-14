-- 爬虫功能配置表
CREATE TABLE `sys_scraper_feature` (
  `id` bigint unsigned NOT NULL COMMENT '数据主键',
  `follower` tinyint(1) NOT NULL COMMENT '是否获取粉丝数',
  `anchor_id` bigint unsigned NOT NULL COMMENT '主播ID',
  `created_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_anchor_id` (`anchor_id`),
  CONSTRAINT `fk_scraper_feature_anchor` FOREIGN KEY (`anchor_id`) REFERENCES `sys_anchor_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫功能配置';
