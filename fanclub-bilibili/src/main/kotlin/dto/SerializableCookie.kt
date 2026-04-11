/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import okhttp3.Cookie

/**
 * 可序列化的 Cookie 数据传输对象
 * 用于解决 okhttp3.Cookie 无法直接序列化的问题
 */
data class SerializableCookie @JsonCreator constructor(
    /**
     * Cookie 名称
     */
    @JsonProperty("name")
    val name: String,

    /**
     * Cookie 值
     */
    @JsonProperty("value")
    val value: String,

    /**
     * Cookie 域名
     */
    @JsonProperty("domain")
    val domain: String,

    /**
     * Cookie 路径
     */
    @JsonProperty("path")
    val path: String = "/",

    /**
     * Cookie 过期时间
     */
    @JsonProperty("expiresAt")
    val expiresAt: Long? = null,

    /**
     * 是否仅在 HTTPS 连接中传输
     */
    @JsonProperty("secure")
    val secure: Boolean = false,

    /**
     * 是否仅通过 HTTP 协议访问
     */
    @JsonProperty("httpOnly")
    val httpOnly: Boolean = true,

    /**
     * 是否仅在主机域名下有效
     */
    @JsonProperty("hostOnly")
    val hostOnly: Boolean = true
) {

    /**
     * 转换为 OkHttp Cookie 对象
     */
    fun toCookie(): Cookie {
        return Cookie.Builder()
            .name(name)
            .value(value)
            .domain(domain)
            .path(path)
            .apply {
                if (expiresAt != null) {
                    expiresAt(expiresAt)
                } else {
                    expiresAt(Long.MAX_VALUE)
                }
            }
            .apply {
                if (secure) secure()
                if (httpOnly) httpOnly()
            }
            .build()
    }

    companion object {
        /**
         * 从 OkHttp Cookie 对象创建 SerializableCookie
         */
        fun fromCookie(cookie: Cookie): SerializableCookie {
            return SerializableCookie(
                name = cookie.name,
                value = cookie.value,
                domain = cookie.domain,
                path = cookie.path,
                expiresAt = cookie.expiresAt,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                hostOnly = cookie.hostOnly
            )
        }
    }
}