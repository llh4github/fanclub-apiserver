/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.props

import llh.fanclubvup.common.BID
import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = PropsKeys.BILI_CLIENT_PROP_KEY)
data class BiliClientProp(
    val uid: BID = 10071860L,
    val buvid: String = "016EE42D-96D5-A092-5F01-D311D0BBAFF772750infoc",
    val cookie: String? = null
)