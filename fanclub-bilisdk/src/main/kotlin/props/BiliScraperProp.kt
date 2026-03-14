/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.props

import llh.fanclubvup.common.BID
import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = PropsKeys.BILI_SCRAPER_PROP_KEY)
data class BiliScraperProp(

    val currentBid: BID = 0L
)
