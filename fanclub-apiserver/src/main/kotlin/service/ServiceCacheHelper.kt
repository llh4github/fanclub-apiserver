/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.CacheKeyPrefix
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

abstract class ServiceCacheHelper {

    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate

    private val mapper = jacksonObjectMapper()
    private val ttlDuration = Duration.ofHours(2)
    private val logger = KotlinLogging.logger {}


    /**
     * 缓存或计算数据
     *
     * spring 的 cacheable 注解老有序列化问题，所以这里自己实现
     * @param localKey 缓存的key
     * @param clazz 缓存的数据类型
     * @param ttl 缓存的过期时间
     * @param computeData 计算数据的方法
     * @return 数据
     */
    protected fun <T> cacheData(
        localKey: String,
        clazz: Class<T>,
        ttl: Duration = ttlDuration,
        computeData: () -> T?
    ): T? {
        val key = CacheKeyPrefix.SERVICE_CACHE_KEY + localKey
        val json = redisTemplate.opsForValue().get(key)
        val rs = try {
            mapper.readValue(json, clazz)
        } catch (e: Exception) {
            logger.error(e) { "反序列化失败: $json" }
            null
        }
        if (rs != null) return rs

        val data = computeData()
        if (data != null)
            redisTemplate.opsForValue().set(key, mapper.writeValueAsString(data), ttl)
        else logger.warn { "$computeData 数据为空不缓存到redis" }
        return data
    }
}
