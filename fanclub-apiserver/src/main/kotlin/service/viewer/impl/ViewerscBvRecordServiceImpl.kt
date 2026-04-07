package llh.fanclubvup.apiserver.service.viewer.impl

import llh.fanclubvup.apiserver.entity.viewer.ViewerScBvRecord
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.viewer.ViewerScBvRecordService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ViewerScBvRecordServiceImpl(
    sqlClient: KSqlClient,
) : ViewerScBvRecordService,
    BaseDatabaseServiceImpl<ViewerScBvRecord>(ViewerScBvRecord::class, sqlClient) {

}
