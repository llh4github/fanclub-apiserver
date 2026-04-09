/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.cache.BiliClientConfigCacheManager
import llh.fanclubvup.bilibili.http.BiliHttpClient
import llh.fanclubvup.bilibili.props.BiliClientConfig
import llh.fanclubvup.bilibili.props.BiliClientConfigImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * B站客户端自动配置类
 * 负责自动配置 B站客户端相关的 Bean
 * 
 * @EnableConfigurationProperties 启用 BiliClientConfigImpl 配置属性
 */
@Configuration
@EnableConfigurationProperties(BiliClientConfigImpl::class)
class BiliClientAutoConfig {
    private val logger = KotlinLogging.logger {}

    /**
     * 创建 BiliClientConfigCacheManager Bean
     * 用于缓存 B站客户端的 cookies 信息
     * 
     * @return BiliClientConfigCacheManager 实例
     */
    @Bean
    fun biliClientConfigCacheManager(): BiliClientConfigCacheManager {
        logger.info { "BiliClientConfigCacheManager init" }
        return BiliClientConfigCacheManager()
    }

    /**
     * 创建 BiliHttpClient Bean
     * 用于发送 HTTP 请求，获取弹幕服务器信息
     * 
     * @param config 客户端配置
     * @return BiliHttpClient 实例
     */
    @Bean
    @ConditionalOnMissingBean(BiliHttpClient::class)
    fun biliHttpClient(config: BiliClientConfig): BiliHttpClient {
        logger.info { "BiliHttpClient init" }
        return BiliHttpClient(config)
    }

    /**
     * 创建 BiliClient Bean
     * 用于管理与 B站服务器的连接
     * 
     * @param config 客户端配置
     * @param httpClient HTTP 客户端
     * @return BiliClient 实例
     */
    @Bean
    @ConditionalOnMissingBean(BiliClient::class)
    fun biliClient(
        config: BiliClientConfig,
        httpClient: BiliHttpClient
    ): BiliClient {
        logger.info { "BiliClient init" }
        return BiliClient(
            roomId = 0L, // 房间 ID 必须由外部指定，这里只是创建一个默认实例
            config = config,
            httpClient = httpClient
        )
    }
}