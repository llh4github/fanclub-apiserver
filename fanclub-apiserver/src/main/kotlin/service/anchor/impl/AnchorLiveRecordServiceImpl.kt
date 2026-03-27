/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.entity.anchor.*
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordEndLiveInput
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.table.makeOrders
import org.springframework.stereotype.Service

@Service
class AnchorLiveRecordServiceImpl(
    sqlClient: KSqlClient,
) : AnchorLiveRecordService,
    BaseDatabaseServiceImpl<AnchorLiveRecord>(AnchorLiveRecord::class, sqlClient) {

    private val logger = KotlinLogging.logger {}
    override fun updateEndLiveStatus(input: AnchorLiveRecordEndLiveInput): Int = sqlClient.transaction {
        val id = createQuery {
            where {
                table.roomId eq input.roomId
                table.isLive eq true
                table.endLiveTime.isNull()
            }
            select(table.id)
        }.fetchFirstOrNull()
        if (id == null) {
            logger.warn { "没有要更新的直播状态： $input" }
            return@transaction 0
        }
        val rs = createUpdate {
            set(table.isLive, false)
            set(table.endLiveTime, input.endLiveTime)
            where {
                table.id eq id
            }
        }.execute()
        return@transaction rs
    }

}
