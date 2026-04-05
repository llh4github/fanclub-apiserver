/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.ksp.annon

import llh.fanclubvup.common.consts.CacheKeyPrefix

/**
 * 缓存名称生成器
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CacheNameGen(
    val prefix: String = CacheKeyPrefix.SERVICE_CACHE_KEY,
)
