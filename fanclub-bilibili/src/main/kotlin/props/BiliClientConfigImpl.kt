/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.props

import llh.fanclubvup.bilibili.dto.SerializableCookie
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.consts.PropsKeys
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * B站客户端配置默认实现
 * 从配置文件中读取配置信息
 * 
 * 使用 @ConfigurationProperties 注解，从 application.yaml 或 application.properties 文件中读取配置
 * 配置前缀为 fanclub.bili.client
 */
@ConfigurationProperties(prefix = PropsKeys.BILI_CLIENT_PROP_KEY)
data class BiliClientConfigImpl(
    /**
     * 用户 ID
     * 默认值：10071860L
     */
    private val uid: BID = 10071860L,
    /**
     * buvid
     * 默认值：016EE42D-96D5-A092-5F01-D311D0BBAFF772750infoc
     */
    private val buvid: String = "016EE42D-96D5-A092-5F01-D311D0BBAFF772750infoc",
    /**
     * cookie 映射
     * 键为 cookie 名称，值为 cookie 值
     */
    private val cookieMap: Map<String, String> = emptyMap()
) : BiliClientConfig {

    /**
     * B站基础 URL
     * 用于提取域名，设置 cookie 的 domain 属性
     */
    private val baseUrl = "https://bilibili.com".toHttpUrlOrNull() ?: "https://bilibili.com".toHttpUrl()

    /**
     * 获取用户 ID
     * 
     * @return 用户 ID
     */
    override fun getUid(): BID {
        return uid
    }

    /**
     * 获取 buvid
     * 
     * @return buvid
     */
    override fun getBuvid(): String {
        return buvid
    }

    /**
     * 获取 cookie 列表
     * 
     * 将配置文件中的 cookieMap 转换为 SerializableCookie 列表
     * 
     * @return cookie 列表
     */
    override fun getCookies(): List<SerializableCookie> {
        return cookieMap.map {
            SerializableCookie(
                name = it.key,
                value = it.value,
                domain = baseUrl.host, // 使用 B站 域名
                path = "/" // 根路径
            )
        }
    }

    /**
     * 刷新配置
     * 
     * 配置文件实现，无需刷新，因为配置文件的更新需要重启应用
     */
    override fun refresh() {
        // 配置文件实现，无需刷新
    }
}