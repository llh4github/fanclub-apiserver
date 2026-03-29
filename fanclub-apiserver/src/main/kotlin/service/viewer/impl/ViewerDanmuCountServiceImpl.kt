package llh.fanclubvup.apiserver.service.viewer.impl

import llh.fanclubvup.apiserver.entity.viewer.ViewerDanmuCount
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerDanmuCountAddInput
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.viewer.ViewerDanmuCountService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ViewerDanmuCountServiceImpl(
    sqlClient: KSqlClient,
) : ViewerDanmuCountService,
    BaseDatabaseServiceImpl<ViewerDanmuCount>(ViewerDanmuCount::class, sqlClient) {

    override fun saveListNoTx(list: List<ViewerDanmuCountAddInput>): Int {
        val entities = list.map { input ->
            input.toEntity {
                bid = input.bid
                cntDate = input.cntDate
                cnt = input.cnt
            }
        }
        return sqlClient.saveEntities(entities).totalAffectedRowCount
    }
}
