/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.cache

import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

class BiliSignCacheManager(private val redisTemplate: StringRedisTemplate) {

    private val defaultExpire = Duration.ofHours(10)

    fun getOrFetch(key: String, expire: Duration = defaultExpire, compute: () -> String?): String? {

        redisTemplate.opsForValue().get(key)?.let {
            return it
        }
        return compute()?.apply {
            set(key, this, expire)
        }

    }

    fun set(key: String, value: String, expire: Duration = defaultExpire) {
        redisTemplate.opsForValue().set(key, value, expire)
    }

    fun remove(key: String) {
        redisTemplate.delete(key)
    }
}
