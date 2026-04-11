/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys

import llh.fanclubvup.apiserver.entity.sys.ScraperCookie
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import llh.fanclubvup.bilibili.props.BiliClientConfig

interface ScraperCookieService : BaseDatabaseService<ScraperCookie> {

    fun fetchRandomCookies(): BiliClientConfig?
}
