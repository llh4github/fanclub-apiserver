/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.viewer.impl

import llh.fanclubvup.apiserver.consts.LongTypeRef
import llh.fanclubvup.apiserver.entity.viewer.ViewerScBvRecord
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvCountSpec
import llh.fanclubvup.apiserver.entity.viewer.id
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.viewer.ViewerScBvRecordService
import llh.fanclubvup.ksp.annon.CacheNameGen
import llh.fanclubvup.ksp.generated.ViewerScBvRecordServiceCacheHelper
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.springframework.stereotype.Service

@Service
class ViewerScBvRecordServiceImpl(
    sqlClient: KSqlClient,
) : ViewerScBvRecordService,
    BaseDatabaseServiceImpl<ViewerScBvRecord>(ViewerScBvRecord::class, sqlClient) {
    @CacheNameGen
    override fun bvCount(spec: ViewerScBvCountSpec): Long {
        return cacheData(
            ViewerScBvRecordServiceCacheHelper.BV_COUNT_CACHE_PREFIX + ":${spec.roomId}:${spec.bv}",
            LongTypeRef,
        ) {
            createQuery {
                where(spec)
                select(count(table.id))
            }.fetchFirst()
        } ?: 0L
    }
}
