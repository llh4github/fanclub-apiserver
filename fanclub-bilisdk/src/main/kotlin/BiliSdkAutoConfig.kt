/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.http.BiliLiveApiClient
import llh.fanclubvup.bilisdk.props.BiliLiveApiProp
import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@ConditionalOnClass(BiliLiveApiProp::class)
@EnableConfigurationProperties(BiliLiveApiProp::class)
class BiliSdkAutoConfig {
    private val logger = KotlinLogging.logger {}

    @Bean
//    @ConditionalOnProperty(prefix = PropsKeys.BILI_SDK_PROP_KEY, name = ["accessKeyId"])
    @ConditionalOnMissingBean(BiliLiveApiClient::class)
    fun biliLiveApiClient(prop: BiliLiveApiProp): BiliLiveApiClient {
        logger.info { "BiliLiveApiClient init" }
        return BiliLiveApiClient(prop)
    }

}
