/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.consts.enums.LiveRecordStatus
import llh.fanclubvup.apiserver.dto.anchor.AnchorLiveDateDurationDto
import llh.fanclubvup.apiserver.entity.anchor.*
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveDurationAddInput
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveDurationDateDuration
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveTimeRecord
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveDurationService
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.between
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.lt
import org.springframework.stereotype.Service
import tools.jackson.core.type.TypeReference
import java.time.Duration
import java.time.LocalDate

@Service
class AnchorLiveDurationServiceImpl(
    sqlClient: KSqlClient,
) : AnchorLiveDurationService,
    BaseDatabaseServiceImpl<AnchorLiveDuration>(AnchorLiveDuration::class, sqlClient) {
    override fun fetchLiveDurationHistory(
        roomId: Long,
        date: LocalDate
    ): List<AnchorLiveDurationDateDuration> {
        return cacheData(
            "AnchorLiveDurationService:fetchLiveDurationHistory:$roomId:$date",
            object : TypeReference<List<AnchorLiveDurationDateDuration>>() {},
            Duration.ofHours(24)
        ) {
            createQuery {
                where(table.roomId.eq(roomId))
                where(table.statDate.lt(date))
                select(table.fetch(AnchorLiveDurationDateDuration::class))
            }.execute()
        } ?: emptyList()
    }

    //#region 计算直播时长
    override fun computeLiveDuration(roomId: Long, date: LocalDate): Int {
        val endLives = sqlClient.createQuery(AnchorLiveRecord::class) {
            where(table.roomId.eq(roomId))
            where(table.liveTime.between(LocalDateTimeUtil.dayOfStart(date), LocalDateTimeUtil.dayOfEnd(date)))
            where(table.liveStatus.eq(LiveRecordStatus.END_LIVING))
            where(table.endLiveTime.isNotNull())
            select(table.fetch(AnchorLiveTimeRecord::class))
        }.execute().flatMap { splitLiveRecordByDate(it) }
            // 合并相同日期的数据，累加时长
            .groupBy { it.liveDate }
            .map { (liveDate, durations) ->
                AnchorLiveDateDurationDto(liveDate, durations.sumOf { it.liveDuration })
            }
            .map { AnchorLiveDurationAddInput(roomId, it.liveDate, it.liveDuration) }
            .toList()
        // 按日期是否等于指定日期分组
        val groupedByDateMap = endLives.groupBy { record ->
            record.statDate == date  // true 或 false 作为 key
        }

        return sqlClient.transaction {
            var total = 0
            val existData = createQuery {
                where(table.roomId.eq(roomId))
                where(table.statDate.eq(date))
                select(table.id, table.liveDuration)
            }.fetchFirstOrNull()
            if (existData != null) {
                val (id, liveDuration) = existData
                groupedByDateMap[true]?.sumOf { it.liveDuration }?.let { durationSum ->
                    total += createUpdate {
                        where(table.id.eq(id))
                        set(table.liveDuration, durationSum + liveDuration)
                    }.execute()
                }
            } else {
                groupedByDateMap[true]?.let {
                    val rs = sqlClient.saveInputsCommand(it).execute()
                    total += rs.totalAffectedRowCount
                }
            }

            groupedByDateMap[false]?.let {
                val rs = sqlClient.saveInputsCommand(it).execute()
                total += rs.totalAffectedRowCount
            }
            total
        }
    }

    /**
     * 将单条直播记录按日期拆分，计算每天的直播时长
     * 如果直播跨天，会将时长分配到对应的日期
     */
    fun splitLiveRecordByDate(record: AnchorLiveTimeRecord): List<AnchorLiveDateDurationDto> {
        val startTime = record.liveTime!!
        val endTime = record.endLiveTime!!

        // 如果开始和结束在同一天，直接返回单条记录
        if (LocalDateTimeUtil.isSameDay(startTime, endTime)) {
            val duration = LocalDateTimeUtil.diffSeconds(startTime, endTime)
            return listOf(AnchorLiveDateDurationDto(startTime.toLocalDate(), duration))
        }

        // 跨天直播：按日期拆分时长
        val result = mutableListOf<AnchorLiveDateDurationDto>()

        // 计算第一天的时长（从开始时间到当天 23:59:59.999）
        val firstDayEnd = LocalDateTimeUtil.dayOfEnd(startTime)
        val firstDayDuration = LocalDateTimeUtil.diffSeconds(startTime, firstDayEnd)
        result.add(AnchorLiveDateDurationDto(startTime.toLocalDate(), firstDayDuration))

        // 从第二天开始遍历
        var currentDate = LocalDateTimeUtil.dayOfStart(startTime).plusDays(1L)

        // 处理中间完整的日期（不包含最后一天）
        while (!LocalDateTimeUtil.isSameDay(currentDate, endTime)) {
            // 完整一天的固定时长为 86400 秒
            result.add(AnchorLiveDateDurationDto(currentDate.toLocalDate(), LocalDateTimeUtil.SECONDS_PER_DAY))
            currentDate = currentDate.plusDays(1L)
        }

        // 处理最后一天的时长（从 00:00:00 到实际结束时间）
        val lastDayDuration = LocalDateTimeUtil.diffSeconds(LocalDateTimeUtil.dayOfStart(endTime), endTime)
        result.add(AnchorLiveDateDurationDto(endTime.toLocalDate(), lastDayDuration))

        return result
    }


//#endregion 计算直播时长
}
