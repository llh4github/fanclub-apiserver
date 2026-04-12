/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components

import llh.fanclubvup.apiserver.utils.JsonUtils
import llh.fanclubvup.common.consts.CacheKeyPrefix
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.scripting.support.ResourceScriptSource
import java.time.Duration


@EnableCaching
@Configuration
class RedisCacheConfig {

    private val mapper = JsonUtils.mapper

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): RedisCacheManager {
        // 默认配置
        val config: RedisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(2)) // 默认缓存 2 小时
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(StringRedisSerializer())
            ) // key 序列化
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(GenericJacksonJsonRedisSerializer(mapper))
            ) // value 序列化
            .disableCachingNullValues() // 不缓存 null 值
            .computePrefixWith { cacheName -> CacheKeyPrefix.SERVICE_CACHE_KEY + cacheName + ":" }
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build()
    }

    @Bean("statisticsDanmu")
    fun statisticsDanmu(): DefaultRedisScript<Boolean> {
        val s = DefaultRedisScript<Boolean>()
        s.resultType = Boolean::class.java
        s.setScriptSource(
            ResourceScriptSource(
                ClassPathResource(
                    "lua/statistics_danmu.lua",
                    this.javaClass.classLoader
                )
            )
        )
        return s
    }


    @Bean("deleteByPattern")
    fun deleteByPattern(): DefaultRedisScript<Long> {
        val s = DefaultRedisScript<Long>()
        s.resultType = Long::class.java
        s.setScriptSource(
            ResourceScriptSource(
                ClassPathResource(
                    "lua/delete_by_pattern.lua",
                    this.javaClass.classLoader
                )
            )
        )
        return s
    }

    @Bean("nicknameChange")
    fun nicknameChange(): DefaultRedisScript<Boolean> {
        val s = DefaultRedisScript<Boolean>()
        s.resultType = Boolean::class.java
        s.setScriptSource(
            ResourceScriptSource(
                ClassPathResource(
                    "lua/nickname_change.lua",
                    this.javaClass.classLoader
                )
            )
        )
        return s
    }
}
