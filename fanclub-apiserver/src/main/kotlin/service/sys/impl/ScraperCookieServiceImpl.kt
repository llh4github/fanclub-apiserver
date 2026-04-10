/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.sys.ScraperCookie
import llh.fanclubvup.apiserver.entity.sys.uid
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.sys.ScraperCookieService
import llh.fanclubvup.bilibili.dto.SerializableCookie
import llh.fanclubvup.bilibili.props.BiliClientConfig
import llh.fanclubvup.ksp.annon.CacheNameGen
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.sql
import org.springframework.stereotype.Service

@Service
class ScraperCookieServiceImpl(
    sqlClient: KSqlClient
) : ScraperCookieService,
    BaseDatabaseServiceImpl<ScraperCookie>(ScraperCookie::class, sqlClient) {
    private val logger = KotlinLogging.logger { }


    //#region 获取随机 Cookie

    @CacheNameGen
    override fun fetchRandomCookies(): BiliClientConfig? {
        val uid = createQuery {
            orderBy(sql(Double::class, "RANDOM()"))
            groupBy(table.uid)
            select(table.uid)
        }.fetchFirstOrNull() ?: return null

        val list = createQuery {
            where { table.uid eq uid }
            select(table)
        }.execute()

        val buvid = list.firstOrNull { it.name == "buvid3" }?.value
        if (buvid == null) {
            logger.error { "Cookie 列表中缺少 buvid3 Cookie" }
            return null
        }
        val sessdata = list.firstOrNull { it.name == "SESSDATA" }
        if (sessdata == null) {
            logger.error { "Cookie 列表中缺少 SESSDATA Cookie" }
            return null
        }

        val cookies = list.map { SerializableCookie(it.name, it.value, it.domain) }.toList()
        return BiliClientConfig(uid, buvid, cookies)
    }
    //#endregion 获取随机 Cookie
}
