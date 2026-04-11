/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.common.consts

/**
 * Cache key 前缀
 */
object CacheKeyPrefix {
    private const val APP_NAME = "fanclub"

    const val SERVICE_CACHE_KEY = "${APP_NAME}-service-layer:"

    const val DYN_IMG_SET_LIKO = "${APP_NAME}:dyn-img-set:liko"
    /**
     * 弹幕统计缓存 key
     */
    const val DANMU_STATS_CACHE_KEY = "${APP_NAME}-statistics:"
}
