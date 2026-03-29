/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.viewer

import llh.fanclubvup.apiserver.entity.viewer.ViewerBasicInfo
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerNicknameUpdateRequest
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface ViewerBasicInfoService : BaseDatabaseService<ViewerBasicInfo> {

    /**
     * 无事务的批量保存
     */
    fun saveListNoTx(list: List<ViewerNicknameUpdateRequest>): Int
}
