/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveRecord
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import org.babyfish.jimmer.sql.KSqlClient
import org.springframework.stereotype.Service

@Service
class AnchorLiveRecordServiceImpl(
    sqlClient: KSqlClient,
) : AnchorLiveRecordService,
    BaseDatabaseServiceImpl<AnchorLiveRecord>(AnchorLiveRecord::class, sqlClient)
