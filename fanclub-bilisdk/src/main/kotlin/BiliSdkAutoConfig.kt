/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.cache.PersistentCookieJarManager
import llh.fanclubvup.bilisdk.dm.cmd.DanmuMsgCommand
import llh.fanclubvup.bilisdk.dm.cmd.SendGiftCommand
import llh.fanclubvup.bilisdk.dm.cmd.SuperChatCommand
import llh.fanclubvup.bilisdk.dm.cmd.UserToastMsgV2Cmd
import llh.fanclubvup.bilisdk.http.BiliLiveApiClient
import llh.fanclubvup.bilisdk.props.BiliLiveApiProp
import llh.fanclubvup.bilisdk.props.BiliScraperProp
import llh.fanclubvup.bilisdk.scraper.BiliScraperClient
import llh.fanclubvup.bilisdk.scraper.BiliWsMsgBizHandler
import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate

@EnableConfigurationProperties(
    value = [
        BiliLiveApiProp::class, BiliScraperProp::class
    ]
)
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
    fun persistentCookieJarManager(redisTemplate: StringRedisTemplate): PersistentCookieJarManager {
        logger.info { "PersistentCookieJarManager init" }
        return PersistentCookieJarManager(redisTemplate)
    }

    @Bean
    @ConditionalOnMissingBean(BiliWsMsgBizHandler::class)
    fun biliWsMsgBizHandler() = object : BiliWsMsgBizHandler {
    }

    @Bean
    @ConditionalOnProperty(
        prefix = PropsKeys.BILI_SCRAPER_PROP_KEY,
        name = ["current-bid"],
        matchIfMissing = false
    )
    @ConditionalOnMissingBean(BiliScraperClient::class)
    fun biliScraperClient(
        redisTemplate: StringRedisTemplate,
        manager: PersistentCookieJarManager,
        prop: BiliScraperProp,
        biliWsMsgBizHandler: BiliWsMsgBizHandler,
    ): BiliScraperClient {
        logger.info { "BiliScraperClient init" }
        return BiliScraperClient(
            BiliSignCacheManager(redisTemplate),
            manager,
            prop,
            biliWsMsgBizHandler
        )
    }
}
