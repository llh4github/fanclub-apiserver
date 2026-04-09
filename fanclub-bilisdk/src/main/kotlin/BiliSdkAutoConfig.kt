/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.cache.BiliSignCacheManager
import llh.fanclubvup.bilisdk.cache.PersistentCookieJarManager
import llh.fanclubvup.bilisdk.http.BiliHttpClient
import llh.fanclubvup.bilisdk.http.BiliLiveApiClient
import llh.fanclubvup.bilisdk.props.BiliLiveApiProp
import llh.fanclubvup.bilisdk.props.BiliScraperProp
import llh.fanclubvup.bilisdk.scraper.*
import llh.fanclubvup.bilisdk.security.WbiSignService
import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
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
    fun biliHttpClient(manager: PersistentCookieJarManager): BiliHttpClient {
        logger.info { "BiliHttpClient init" }
        return BiliHttpClient(manager)
    }

    @Bean
    fun wbiSignService(
        redisTemplate: StringRedisTemplate,
        httpClient: BiliHttpClient
    ): WbiSignService {
        logger.info { "WbiSignService init" }
        return WbiSignService(BiliSignCacheManager(redisTemplate), httpClient)
    }

    @Bean
    fun danmuCommandDispatcher(
        handlers: ObjectProvider<DanmuCommandHandler<*>>
    ): DanmuCommandDispatcher {
        logger.info { "DanmuCommandDispatcher init with ${handlers.orderedStream().count()} handlers" }
        return DanmuCommandDispatcher(handlers.orderedStream().toList())
    }

    @Bean
    @ConditionalOnMissingBean(BiliWsMsgBizHandler::class)
    fun biliWsMsgBizHandler(dispatcher: DanmuCommandDispatcher): BiliWsMsgBizHandler {
        logger.info { "DefaultBiliWsMsgBizHandler init" }
        return DefaultBiliWsMsgBizHandler(dispatcher)
    }

    @Bean
    @ConditionalOnProperty(
        prefix = PropsKeys.BILI_SCRAPER_PROP_KEY,
        name = ["current-bid"],
        matchIfMissing = true
    )
    @ConditionalOnMissingBean(BiliScraperClient::class)
    fun biliScraperClient(
        httpClient: BiliHttpClient,
        wbiSignService: WbiSignService,
        prop: BiliScraperProp,
        biliWsMsgBizHandler: BiliWsMsgBizHandler,
        applicationEventPublisher: ApplicationEventPublisher,
        authFetcher: BiliWsAuthFetcher,
    ): BiliScraperClient {
        logger.info { "BiliScraperClient init" }
        return BiliScraperClient(
            httpClient,
            wbiSignService,
            prop,
            biliWsMsgBizHandler,
            applicationEventPublisher,
            authFetcher
        )
    }
}
