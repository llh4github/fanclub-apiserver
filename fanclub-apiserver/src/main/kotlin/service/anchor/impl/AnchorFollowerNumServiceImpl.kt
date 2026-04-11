/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorFollowerNum
import llh.fanclubvup.apiserver.entity.anchor.biliId
import llh.fanclubvup.apiserver.entity.anchor.cntDate
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNum
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNumQuerySpec
import llh.fanclubvup.apiserver.entity.anchor.followerNum
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.lt
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import tools.jackson.core.type.TypeReference
import java.time.Duration
import java.time.LocalDate

@Service
class AnchorFollowerNumServiceImpl(
    sqlClient: KSqlClient,
) : AnchorFollowerNumService,
    BaseDatabaseServiceImpl<AnchorFollowerNum>(AnchorFollowerNum::class, sqlClient) {

    @Cacheable(cacheNames = ["AnchorFollowerNumService:queryNum"], key = "#spec.biliId +':'+ #spec.cntDate")
    override fun queryNum(spec: AnchorFollowerDateNumQuerySpec): Int {
        return createQuery {
            where(spec)
            select(table.followerNum)
        }.fetchFirstOrNull() ?: 0
    }

    override fun queryHistoryNum(
        biliId: BID,
        cntDate: LocalDate
    ): List<AnchorFollowerDateNum> {
        return cacheData(
            "AnchorFollowerNumService:queryHistoryNum:$biliId:$cntDate",
            object : TypeReference<List<AnchorFollowerDateNum>>() {},
            Duration.ofHours(24)
        ) {
            createQuery {
                orderBy(table.cntDate.desc())
                where(table.biliId.eq(biliId))
                where(table.cntDate.lt(cntDate))
                select(table.fetch(AnchorFollowerDateNum::class))
            }.execute()
        } ?: emptyList()
    }
}
