/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.viewer.impl

import llh.fanclubvup.apiserver.entity.viewer.ViewerBasicInfo
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerNicknameUpdateRequest
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.viewer.ViewerBasicInfoService
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ViewerBasicInfoServiceImpl(
    sqlClient: KSqlClient,
) : ViewerBasicInfoService,
    BaseDatabaseServiceImpl<ViewerBasicInfo>(ViewerBasicInfo::class, sqlClient) {

    override fun saveListNoTx(list: List<ViewerNicknameUpdateRequest>): Int {
        val entities = list.map { input ->
            input.toEntity {
                nickname = input.nickname
            }
        }
        return sqlClient.saveEntities(entities).totalAffectedRowCount
    }
}
