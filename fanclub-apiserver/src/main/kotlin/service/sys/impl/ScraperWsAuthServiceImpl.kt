/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys.impl

import llh.fanclubvup.apiserver.entity.sys.ScraperWsAuth
import llh.fanclubvup.apiserver.entity.sys.auth
import llh.fanclubvup.apiserver.entity.sys.roomId
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.sys.ScraperWsAuthService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service

@Service
class ScraperWsAuthServiceImpl(
    sqlClient: KSqlClient,
) : ScraperWsAuthService,
    BaseDatabaseServiceImpl<ScraperWsAuth>(ScraperWsAuth::class, sqlClient) {

    override fun fetch(roomId: Long): String? {
        return createQuery {
            where { table.roomId.eq(roomId) }
            select(table.auth)
        }.fetchFirstOrNull()
    }

}
