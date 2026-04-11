/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.dto.SerializableCookie
import llh.fanclubvup.bilibili.utils.JsonUtils
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import tools.jackson.core.type.TypeReference
import java.time.Duration

/**
 * B站客户端配置缓存管理器
 * 用于缓存 B站客户端的 cookies 信息
 */
class BiliClientConfigCacheManager {

    private val logger = KotlinLogging.logger {}
    private val timeout = Duration.ofHours(10)
    private val COOKIES_KEY = "fanclub:bili:client:cookies"
    private val mapper = JsonUtils.mapper

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache("bili-client-config") {
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String::class.java,
                String::class.java,
                ResourcePoolsBuilder.heap(100)
            )
                .withExpiry(
                    ExpiryPolicyBuilder.timeToLiveExpiration(timeout)
                )
                .build()
        }
        .build(true)

    private val cache by lazy {
        cacheManager.getCache("bili-client-config", String::class.java, String::class.java)
    }

    private var fetchCookieOperation: () -> List<Cookie> = {
        emptyList()
    }

    /**
     * 保存从响应中获取的 cookies
     */
    fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        val existingCookies = fetchCookies().associateBy { it.name }.toMutableMap()
        cookies.forEach { cookie ->
            existingCookies[cookie.name] = cookie
        }

        val allCookies = existingCookies.values.toList()
        val list = allCookies.map { SerializableCookie.fromCookie(it) }
        val value = mapper.writeValueAsString(list)
        cache.put(COOKIES_KEY, value)
    }

    /**
     * 重置 cookies
     */
    fun resetCookies(block: () -> List<Cookie>) {
        fetchCookieOperation = block
        cacheCookies()
    }

    /**
     * 缓存 cookies
     */
    private fun cacheCookies(): List<Cookie> {
        val cookies = fetchCookieOperation()
        if (cookies.isEmpty()) {
            logger.error { "没有找到 cookie 数据" }
            return emptyList()
        }
        val list = cookies.map { SerializableCookie.fromCookie(it) }
        val value = mapper.writeValueAsString(list)
        logger.debug { "resetCookies: \n$list \n$value" }
        cache.put(COOKIES_KEY, value)
        return cookies
    }

    /**
     * 加载请求所需的 cookies
     */
    fun loadForRequest(url: HttpUrl): List<Cookie> = fetchCookies()

    /**
     * 从缓存中获取 cookies
     */
    fun fetchCookies(): List<Cookie> {
        val cookies = cache.get(COOKIES_KEY) ?: return cacheCookies()
        return mapper.readValue(cookies, object : TypeReference<List<SerializableCookie>>() {})
            .map { it.toCookie() }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        cache.remove(COOKIES_KEY)
    }
}