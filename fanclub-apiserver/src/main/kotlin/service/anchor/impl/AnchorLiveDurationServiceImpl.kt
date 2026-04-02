/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveDuration
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveDurationService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class AnchorLiveDurationServiceImpl(
    sqlClient: KSqlClient,
) : AnchorLiveDurationService,
    BaseDatabaseServiceImpl<AnchorLiveDuration>(AnchorLiveDuration::class, sqlClient) {

}
