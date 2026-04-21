/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorFollowerNum
import llh.fanclubvup.apiserver.entity.anchor.bid
import llh.fanclubvup.apiserver.entity.anchor.cntDate
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNum
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNumQuerySpec
import llh.fanclubvup.apiserver.entity.anchor.followerNum
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import llh.fanclubvup.common.BID
import llh.fanclubvup.ksp.annon.CacheNameGen
import llh.fanclubvup.ksp.generated.AnchorFollowerNumServiceCacheHelper
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.lt
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AnchorFollowerNumServiceImpl(
    sqlClient: KSqlClient,
) : AnchorFollowerNumService,
    BaseDatabaseServiceImpl<AnchorFollowerNum>(AnchorFollowerNum::class, sqlClient) {

    @CacheNameGen
    override fun queryNum(spec: AnchorFollowerDateNumQuerySpec): Int {
        val prefix = AnchorFollowerNumServiceCacheHelper.QUERY_NUM_CACHE_PREFIX
        return cacheData(
            "$prefix:${spec.bid}:${spec.cntDate}",
            AnchorFollowerNumServiceCacheHelper.QueryNumTypeRef,
        ) {
            createQuery {
                where(spec)
                select(table.followerNum)
            }.fetchFirstOrNull() ?: 0
        } ?: 0
    }

    @CacheNameGen
    override fun queryHistoryNum(
        biliId: BID,
        cntDate: LocalDate
    ): List<AnchorFollowerDateNum> {
        val prefix = AnchorFollowerNumServiceCacheHelper.QUERY_HISTORY_NUM_CACHE_PREFIX
        return cacheData(
            "$prefix:$biliId:$cntDate",
            AnchorFollowerNumServiceCacheHelper.QueryHistoryNumTypeRef,
        ) {
            createQuery {
                orderBy(table.cntDate.desc())
                where(table.bid.eq(biliId))
                where(table.cntDate.lt(cntDate))
                select(table.fetch(AnchorFollowerDateNum::class))
            }.execute()
        } ?: emptyList()
    }
}
