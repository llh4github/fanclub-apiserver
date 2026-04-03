/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys.impl

import llh.fanclubvup.apiserver.entity.sys.ScraperFeature
import llh.fanclubvup.apiserver.entity.sys.dto.ScraperEnableFeatureEnabledView
import llh.fanclubvup.apiserver.entity.sys.follower
import llh.fanclubvup.apiserver.entity.sys.monitor
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service
import tools.jackson.core.type.TypeReference

@Service
class ScraperFeatureServiceImpl(
    sqlClient: KSqlClient,
) : ScraperFeatureService,
    BaseDatabaseServiceImpl<ScraperFeature>(ScraperFeature::class, sqlClient) {
    override fun queryFollowerEnabled(): List<ScraperEnableFeatureEnabledView> {
        return cacheData(
            "ScraperFeatureService:queryFollowerEnabled",
            object : TypeReference<List<ScraperEnableFeatureEnabledView>>() {}
        ) {
            createQuery {
                where { table.follower eq true }
                select(table.fetch(ScraperEnableFeatureEnabledView::class))
            }.execute()
        } ?: emptyList()
    }

    override fun queryMonitorEnabled(): List<ScraperEnableFeatureEnabledView> {
        return cacheData(
            "ScraperFeatureService:queryMonitorEnabled",
            object : TypeReference<List<ScraperEnableFeatureEnabledView>>() {}
        ) {
            createQuery {
                where { table.monitor eq true }
                select(table.fetch(ScraperEnableFeatureEnabledView::class))
            }.execute()
        } ?: emptyList()
    }
}
