/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.props

import llh.fanclubvup.bilibili.dto.SerializableCookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * B站客户端配置工厂方法
 * 从配置文件中的 cookieMap 创建 BiliClientConfigFetcher 实例
 * 
 * 此函数提供向后兼容的配置创建方式，支持从 Map 格式的配置数据构建配置对象
 * 
 * @param uid 用户 ID
 * @param buvid 设备唯一标识
 * @param cookieMap Cookie 映射，键为 cookie 名称，值为 cookie 值
 * @return BiliClientConfigFetcher 实例
 */
fun createBiliClientConfigFromMap(
    uid: Long = -1L,
    buvid: String = "",
    cookieMap: Map<String, String> = emptyMap()
): BiliClientConfig {
    val baseUrl = "https://bilibili.com".toHttpUrlOrNull() ?: "https://bilibili.com".toHttpUrl()

    val cookies = cookieMap.map { (name, value) ->
        SerializableCookie(
            name = name,
            value = value,
            domain = baseUrl.host,
            path = "/"
        )
    }

    return BiliClientConfig(
        uid = uid,
        buvid = buvid,
        cookies = cookies
    )
}