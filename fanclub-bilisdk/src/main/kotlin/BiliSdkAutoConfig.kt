/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.cache.PersistentCookieJarManager
import llh.fanclubvup.bilisdk.http.BiliLiveApiClient
import llh.fanclubvup.bilisdk.props.BiliLiveApiProp
import llh.fanclubvup.bilisdk.scraper.BiliScraperClient
import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate

@EnableConfigurationProperties(BiliLiveApiProp::class)
class BiliSdkAutoConfig {
    private val logger = KotlinLogging.logger {}

    @Bean
    @ConditionalOnProperty(
        prefix = PropsKeys.BILI_SDK_PROP_KEY,
        name = ["access-key-id"],
        matchIfMissing = false
    )
    @ConditionalOnMissingBean(BiliLiveApiClient::class)
    fun biliLiveApiClient(prop: BiliLiveApiProp): BiliLiveApiClient {
        logger.info { "BiliLiveApiClient init" }
        return BiliLiveApiClient(prop)
    }


    @Bean
    fun biliScraperClient(redisTemplate: StringRedisTemplate): BiliScraperClient {
        logger.info { "BiliScraperClient init" }
        return BiliScraperClient(BiliSignCacheManager(redisTemplate))
    }

    @Bean
    fun persistentCookieJarManager(redisTemplate: StringRedisTemplate): PersistentCookieJarManager {
        logger.info { "PersistentCookieJarManager init" }
        return PersistentCookieJarManager(redisTemplate)
    }
}
