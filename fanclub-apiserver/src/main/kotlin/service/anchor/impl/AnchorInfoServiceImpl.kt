/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorInfo
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorInfoService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class AnchorInfoServiceImpl(
    sqlClient: KSqlClient,
) : AnchorInfoService,
    BaseDatabaseServiceImpl<AnchorInfo>(AnchorInfo::class, sqlClient) {

}
