/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.entity.anchor.AnchorFollowerNum
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNum
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNumQuerySpec
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import llh.fanclubvup.common.BID
import java.time.LocalDate
import kotlin.reflect.KProperty1

interface AnchorFollowerNumService : BaseDatabaseService<AnchorFollowerNum> {

    companion object {
        val defaultUniqueKeys: List<KProperty1<AnchorFollowerNum, *>> =
            listOf(AnchorFollowerNum::bid, AnchorFollowerNum::cntDate)
    }

    /**
     * 查询指定时间段内粉丝数
     */
    fun queryNum(spec: AnchorFollowerDateNumQuerySpec): Int

    /**
     * 查询指定时间之前的粉丝数,不包含[cntDate]的数据
     *
     * 后面数据量上来了得改实现方法
     */
    fun queryHistoryNum(biliId: BID, cntDate: LocalDate): List<AnchorFollowerDateNum>
}
