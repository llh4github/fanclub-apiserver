/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.cache

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.time.Duration
import kotlin.jvm.java

object EhcaheHashcodeExpiryManager {
    private const val CACHE_NAME = "string-hashcode"
    private val cacheManager =
        CacheManagerBuilder.newCacheManagerBuilder()
            .withCache(CACHE_NAME) {
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    String::class.java,           // 键类型
                    Int::class.java,           // 值类型
                    ResourcePoolsBuilder.heap(1000)  // 堆内存储，最多1000个条目
                )
                    .withExpiry(
                        ExpiryPolicyBuilder.timeToLiveExpiration(
                            Duration.ofSeconds(10)  // 过期
                        )
                    )
                    .build()
            }
            .build(true)

    private val cache by lazy {
        cacheManager.getCache(CACHE_NAME, String::class.java, Int::class.java)
    }

    fun putIfAbsent(str: String): Int? {
        return cache.putIfAbsent(str, str.hashCode())
    }
}
