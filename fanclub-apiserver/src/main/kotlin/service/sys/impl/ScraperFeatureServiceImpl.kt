/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys.impl

import llh.fanclubvup.apiserver.entity.sys.ScraperFeature
import llh.fanclubvup.apiserver.entity.sys.dto.ScraperFeatureFollowerEnabledView
import llh.fanclubvup.apiserver.entity.sys.follower
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service

@Service
class ScraperFeatureServiceImpl(
    sqlClient: KSqlClient,
) : ScraperFeatureService,
    BaseDatabaseServiceImpl<ScraperFeature>(ScraperFeature::class, sqlClient) {
    override fun queryFollowerEnabled(): List<ScraperFeatureFollowerEnabledView> {
        return createQuery {
            where { table.follower eq true }
            select(table.fetch(ScraperFeatureFollowerEnabledView::class))
        }.execute()
    }
}
