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
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveRecordQueryWeekSpec
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveTimeRecord
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveRecordService
import llh.fanclubvup.common.consts.CacheKeyPrefix
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import llh.fanclubvup.ksp.annon.CacheNameGen
import llh.fanclubvup.ksp.generated.AnchorLiveRecordServiceCacheHelper
import llh.fanclubvup.ksp.generated.AnchorLiveRecordServiceCacheHelper.FETCH_END_LIVE_RECORD_CACHE_PREFIX
import llh.fanclubvup.ksp.generated.AnchorLiveRecordServiceCacheHelper.FETCH_WEEK_LIVE_RECORD_CACHE_PREFIX
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import tools.jackson.core.type.TypeReference
import java.time.Duration
import java.time.LocalDateTime

@Service
class AnchorLiveRecordServiceImpl(
    sqlClient: KSqlClient,
) : AnchorLiveRecordService,
    BaseDatabaseServiceImpl<AnchorLiveRecord>(AnchorLiveRecord::class, sqlClient) {

    private val logger = KotlinLogging.logger {}

    @Autowired
    @Qualifier("deleteByPattern")
    private lateinit var deleteByPattern: DefaultRedisScript<Long>

    override fun updateEndLiveStatus(input: AnchorLiveRecordEndLiveInput): Int = sqlClient.transaction {
        val tuple = createQuery {
            where(
                table.roomId eq input.roomId,
                table.liveStatus eq LiveRecordStatus.LIVING,
                table.endLiveTime.isNull()
            )
            select(table.id, table.liveTime, table.roomId)
        }.fetchFirstOrNull()
        if (tuple == null) {
            logger.warn { "没有要更新的直播状态： $input" }
            return@transaction 0
        }

        val endTime = input.endLiveTime ?: LocalDateTime.now()
        val dur = Duration.between(tuple._2, endTime)
        val rs = createUpdate {
            set(table.liveStatus, LiveRecordStatus.END_LIVING)
            set(table.liveDuration, dur.seconds)
            set(table.endLiveTime, endTime)
            where {
                table.id eq tuple._1
            }
        }.execute()

        // 清缓存
        executorVirtualThread.execute {
            val keys = listOf(
                CacheKeyPrefix.SERVICE_CACHE_KEY + "AnchorLiveRecordService:fetchLiveStatus:" + tuple._3,
                CacheKeyPrefix.SERVICE_CACHE_KEY + "AnchorLiveRecordService:fetchEndLiveRecord:" + tuple._3 + ":*"
            )
            val deleted = redisTemplate.execute(deleteByPattern, keys, "")

            logger.debug { "删除缓存 $deleted 条" }
        }
        return@transaction rs
    }

    override fun finishLiveForOvertime() {
        val list = createQuery {
            where(table.liveStatus eq LiveRecordStatus.LIVING)
            where(
                sql(Boolean::class, "NOW() - %e >= INTERVAL '18 hours'") {
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
            set(table.liveStatus, LiveRecordStatus.OVER_TIME_END)
            set(table.endLiveTime, LocalDateTime.now())
            where(table.id valueIn list)
        }.execute()

        logger.info { "更新了 $cnt 条超时直播状态数据" }
    }

    override fun fetchLiveStatus(roomId: Long): AnchorLiveRecordLiveStatus {
        return cacheData(
            "AnchorLiveRecordService:fetchLiveStatus:$roomId",
            object : TypeReference<AnchorLiveRecordLiveStatus>() {}
        ) {
            createQuery {
                orderBy(table.liveTime.desc())
                where { table.roomId eq roomId }
                select(table.fetch(AnchorLiveRecordLiveStatus::class))
            }.fetchFirstOrNull()
        } ?: AnchorLiveRecordLiveStatus(null, LiveRecordStatus.UNKNOWN)
    }

    @CacheNameGen
    override fun fetchEndLiveRecord(
        roomId: Long,
        last: Int
    ): List<AnchorLiveRecordLiveStatus> {
        val key = "$FETCH_END_LIVE_RECORD_CACHE_PREFIX:$roomId:$last"
        return cacheData(
            key,
            AnchorLiveRecordServiceCacheHelper.FetchEndLiveRecordTypeRef,
        ) {
            createQuery {
                orderBy(table.liveTime.desc())
                where { table.roomId eq roomId }
                where(table.liveStatus eq LiveRecordStatus.END_LIVING)
                select(table.fetch(AnchorLiveRecordLiveStatus::class))
            }.limit(last).execute()
        } ?: emptyList()
    }

    @CacheNameGen
    override fun fetchWeekLiveRecord(spec: AnchorLiveRecordQueryWeekSpec): List<AnchorLiveTimeRecord> {
        val key = "$FETCH_WEEK_LIVE_RECORD_CACHE_PREFIX:${spec.roomId}:${spec.week}"
        return cacheData(
            key,
            AnchorLiveRecordServiceCacheHelper.FetchWeekLiveRecordTypeRef
        ) {
            val range = LocalDateTimeUtil.weekRange(spec.week)
            createQuery {
                where(table.liveTime.between(range.first, range.second))
                where(table.roomId eq spec.roomId)
                where(table.liveStatus.eq(LiveRecordStatus.END_LIVING))
                select(table.fetch(AnchorLiveTimeRecord::class))
            }.execute()
        } ?: emptyList()
    }
}
