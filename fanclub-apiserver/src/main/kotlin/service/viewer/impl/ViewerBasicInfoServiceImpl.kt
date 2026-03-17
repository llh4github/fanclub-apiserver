package llh.fanclubvup.apiserver.service.viewer.impl

import llh.fanclubvup.apiserver.entity.viewer.ViewerBasicInfo
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.viewer.ViewerBasicInfoService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ViewerBasicInfoServiceImpl(
    sqlClient: KSqlClient,
) : ViewerBasicInfoService,
    BaseDatabaseServiceImpl<ViewerBasicInfo>(ViewerBasicInfo::class, sqlClient)
