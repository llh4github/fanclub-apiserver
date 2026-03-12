/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.cache

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.BiliSdkCacheKey
import llh.fanclubvup.bilisdk.dto.SerializableCookie
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.springframework.data.redis.core.StringRedisTemplate
import tools.jackson.core.type.TypeReference
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

class PersistentCookieJarManager(private val redisTemplate: StringRedisTemplate) : CookieJar {

    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()
    private val timeout = Duration.ofHours(10)

    private val localCache by lazy {
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .expireAfterAccess(Duration.ofMinutes(5))
            .build<String, List<Cookie>>()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
//        redisTemplate.opsForValue().set(BiliSdkCacheKey.COOKIES, mapper.writeValueAsString(cookies))
//        localCache.put(BiliSdkCacheKey.COOKIES, cookies)
        //TODO 测试有没有必要缓存下来
        logger.info { "saveFromResponse: $url, $cookies" }
    }

    fun resetCookies(block: () -> List<Cookie>) {
        val cookies = block()
        val value = mapper.writeValueAsString(cookies.map { SerializableCookie.fromCookie(it) }.toList())
        redisTemplate.opsForValue().set(BiliSdkCacheKey.COOKIES, value, timeout)
        localCache.put(BiliSdkCacheKey.COOKIES, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = fetchCookies()

    fun fetchCookies() = localCache.get(BiliSdkCacheKey.COOKIES) { _ ->
        val cookies = redisTemplate.opsForValue().get(BiliSdkCacheKey.COOKIES) ?: return@get emptyList()
        val list = mapper.readValue(cookies, object : TypeReference<List<SerializableCookie>>() {})
            .map { it.toCookie() }
        localCache.put(BiliSdkCacheKey.COOKIES, list)
        list
    }

}
