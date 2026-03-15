/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.BiliSdkCacheKey
import llh.fanclubvup.bilisdk.dto.SerializableCookie
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.springframework.data.redis.core.StringRedisTemplate
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

class PersistentCookieJarManager(private val redisTemplate: StringRedisTemplate) : CookieJar {

    private val logger = KotlinLogging.logger {}
    private val mapper: ObjectMapper = jacksonObjectMapper()
    private val timeout = Duration.ofHours(10)

    private var fetchCookieOperation: () -> List<Cookie> = {
        emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
//        redisTemplate.opsForValue().set(BiliSdkCacheKey.COOKIES, mapper.writeValueAsString(cookies))
//        localCache.put(BiliSdkCacheKey.COOKIES, cookies)
        //TODO 测试有没有必要缓存下来
        logger.info { "saveFromResponse: $url, $cookies" }
    }

    fun resetCookies(block: () -> List<Cookie>) {
        fetchCookieOperation = block
        cacheCookies()
    }

    private fun cacheCookies(): List<Cookie> {
        val cookies = fetchCookieOperation()
        if (cookies.isEmpty()) {
            logger.error { "没有找cookie数据" }
            return emptyList()
        }
        val list = cookies.map { SerializableCookie.fromCookie(it) }.toList()
        val value = mapper.writeValueAsString(list)
        logger.info { "resetCookies: \n$list \n$value" }
        redisTemplate.opsForValue().set(BiliSdkCacheKey.COOKIES, value, timeout)
        return cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = fetchCookies()

    fun fetchCookies(): List<Cookie> {
        val cookies = redisTemplate.opsForValue().get(BiliSdkCacheKey.COOKIES) ?: return cacheCookies()
        return mapper.readValue(cookies, object : TypeReference<List<SerializableCookie>>() {})
            .map { it.toCookie() }
    }

}
