/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.props

import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = PropsKeys.BILI_SDK_PROP_KEY)
data class BiliLiveApiProp(
    /**
     * 访问密钥
     */
    val accessKeyId: String = "",

    /**
     * 签名密钥
     */
    val signatureKey: String = "",

    /**
     * appId
     */
    val appId: Long = 0L
)
