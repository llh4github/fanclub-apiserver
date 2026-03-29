/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.consts.enums.LiveRecordStatus
import llh.fanclubvup.apiserver.entity.anchor.*
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordEndLiveInput
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordLiveStatus
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AnchorLiveRecordServiceImpl(
    sqlClient: KSqlClient,
) : AnchorLiveRecordService,
    BaseDatabaseServiceImpl<AnchorLiveRecord>(AnchorLiveRecord::class, sqlClient) {

    private val logger = KotlinLogging.logger {}
    override fun updateEndLiveStatus(input: AnchorLiveRecordEndLiveInput): Int = sqlClient.transaction {
        val id = createQuery {
            where(
                table.roomId eq input.roomId,
                table.liveStatus eq LiveRecordStatus.LIVING,
                table.endLiveTime.isNull()
            )
            select(table.id)
        }.fetchFirstOrNull()
        if (id == null) {
            logger.warn { "没有要更新的直播状态： $input" }
            return@transaction 0
        }
        val rs = createUpdate {
            set(table.liveStatus, LiveRecordStatus.NOT_LIVING)
            set(table.endLiveTime, input.endLiveTime)
            where {
                table.id eq id
            }
        }.execute()
        return@transaction rs
    }

    override fun finishLiveForOvertime() {
        val list = createQuery {
            where(table.liveStatus eq LiveRecordStatus.LIVING)
            where(
                sql(Boolean::class, "TIMESTAMPDIFF(HOUR, %e, NOW()) >= 18") {
                    expression(table.liveTime)
                }
            )
            select(table.id)
        }.execute()
        if (list.isEmpty()) {
            logger.info { "没有需要更新的直播状态数据" }
            return
        }
        val cnt = createUpdate {
            set(table.liveStatus, LiveRecordStatus.OVER_TIME)
            set(table.endLiveTime, LocalDateTime.now())
            where(table.id valueIn list)
        }.execute()

        logger.info { "更新了 $cnt 条超时直播状态数据" }
    }

    override fun fetchLiveStatus(roomId: Long): AnchorLiveRecordLiveStatus {
        return cacheData(
            "AnchorLiveRecordService:fetchLiveStatus:$roomId",
            AnchorLiveRecordLiveStatus::class.java
        ) {
            createQuery {
                orderBy(table.updatedTime.desc())
                where { table.roomId eq roomId }
                select(table.fetch(AnchorLiveRecordLiveStatus::class))
            }.fetchFirstOrNull()
        } ?: AnchorLiveRecordLiveStatus(null, LiveRecordStatus.UNKNOWN)
    }
}
