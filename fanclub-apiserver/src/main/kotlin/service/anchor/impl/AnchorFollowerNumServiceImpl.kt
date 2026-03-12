package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorFollowerNum
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNumQuerySpec
import llh.fanclubvup.apiserver.entity.anchor.followerNum
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class AnchorFollowerNumServiceImpl(
    sqlClient: KSqlClient,
) : AnchorFollowerNumService,
    BaseDatabaseServiceImpl<AnchorFollowerNum>(AnchorFollowerNum::class, sqlClient) {

    override fun queryNum(spec: AnchorFollowerDateNumQuerySpec): Int {
        return createQuery {
            where(spec)
            select(table.followerNum)
        }.fetchFirstOrNull() ?: 0
    }

    override fun fetchFollowerNumEnabled(): List<BID> {
        TODO("Not yet implemented")
    }
}
