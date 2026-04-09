/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.websocket

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.time.Duration

/**
 * 消息去重缓存管理器
 * 用于存储消息的哈希码，过期时间为3秒
 */
object MessageDeduplicationCache {
    // 空值对象
    object EmptyValue

    private const val CACHE_NAME = "message-deduplication"
    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(CACHE_NAME) {
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String::class.java,           // 键类型：消息哈希码
                EmptyValue::class.java,       // 值类型：空值
                ResourcePoolsBuilder.heap(1000)  // 堆内存储，最多1000个条目
            )
                .withExpiry(
                    ExpiryPolicyBuilder.timeToLiveExpiration(
                        Duration.ofSeconds(3)  // 过期时间：3秒
                    )
                )
                .build()
        }
        .build(true)

    private val cache by lazy {
        cacheManager.getCache(CACHE_NAME, String::class.java, EmptyValue::class.java)
    }

    /**
     * 检查消息是否在3秒内已处理过
     * @param message 消息内容
     * @return true 表示消息已处理过，false 表示消息未处理过
     */
    fun isDuplicate(message: String): Boolean {
        // 使用消息内容的哈希码作为缓存键
        val key = message.hashCode().toString()
        // 如果缓存中已存在，则表示消息已处理过
        return cache.putIfAbsent(key, EmptyValue) != null
    }
}
