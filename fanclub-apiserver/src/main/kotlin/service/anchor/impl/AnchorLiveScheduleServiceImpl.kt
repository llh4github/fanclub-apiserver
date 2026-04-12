/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveSchedule
import llh.fanclubvup.apiserver.entity.anchor.bid
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveScheduleAddInput
import llh.fanclubvup.apiserver.entity.anchor.id
import llh.fanclubvup.apiserver.entity.anchor.startTime
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveScheduleService
import llh.fanclubvup.common.BID
import llh.fanclubvup.common.utils.LocalDateTimeUtil
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.between
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AnchorLiveScheduleServiceImpl(
    sqlClient: KSqlClient,
) : AnchorLiveScheduleService,
    BaseDatabaseServiceImpl<AnchorLiveSchedule>(AnchorLiveSchedule::class, sqlClient) {

    override fun saveList(input: List<AnchorLiveScheduleAddInput>): Int {
        return sqlClient.transaction {
            val rs = sqlClient.saveInputs(input){
                setMode(SaveMode.INSERT_ONLY)
            }
            rs.totalAffectedRowCount
        }
    }

    override fun hasSchedule(bId: BID, targetWeek: LocalDate): Boolean {
        val range = LocalDateTimeUtil.weekRange(targetWeek)
        return createQuery {
            where(table.bid.eq(bId))
            where(table.startTime.between(range.first, range.second))
            select(table.id)
        }.exists()
    }
}
