/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.viewer.impl

import llh.fanclubvup.apiserver.entity.viewer.ViewerGuardBuyRecord
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.viewer.ViewerGuardBuyRecordService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ViewerGuardBuyRecordServiceImpl(
    sqlClient: KSqlClient,
) : ViewerGuardBuyRecordService,
    BaseDatabaseServiceImpl<ViewerGuardBuyRecord>(ViewerGuardBuyRecord::class, sqlClient)
