/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys.impl

import llh.fanclubvup.apiserver.entity.sys.ScraperCookie
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.sys.ScraperCookieService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ScraperCookieServiceImpl(
    sqlClient: KSqlClient
) : ScraperCookieService,
    BaseDatabaseServiceImpl<ScraperCookie>(ScraperCookie::class, sqlClient) {

}