package llh.fanclubvup.apiserver.service.viewer.impl

import llh.fanclubvup.apiserver.entity.viewer.ViewerScBvRecord
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewScBvCountSpec
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.viewer.ViewerScBvRecordService
import llh.fanclubvup.ksp.annon.CacheNameGen
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ViewerScBvRecordServiceImpl(
    sqlClient: KSqlClient,
) : ViewerScBvRecordService,
    BaseDatabaseServiceImpl<ViewerScBvRecord>(ViewerScBvRecord::class, sqlClient) {
    @CacheNameGen
    override fun bvCount(spec: ViewScBvCountSpec): Long {
//        return cacheData(
//            ViewerScBvRecordServiceCacheHelper.BvCountCachePrefix,
//            ViewerScBvRecordServiceCacheHelper.BvCountCachePrefix,
//            ){
//            createQuery {
//                where(spec)
//                select(count(table.id))
//            }.fetchOne()
//        }?:0L
        TODO()
    }
}
