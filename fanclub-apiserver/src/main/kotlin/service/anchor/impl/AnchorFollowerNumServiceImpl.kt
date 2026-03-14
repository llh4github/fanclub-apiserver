package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorFollowerNum
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNumQuerySpec
import llh.fanclubvup.apiserver.entity.anchor.followerNum
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

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

}
