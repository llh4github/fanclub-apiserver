/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.viewer

import llh.fanclubvup.apiserver.entity.viewer.ViewerScBvRecord
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerScBvCountSpec
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface ViewerScBvRecordService : BaseDatabaseService<ViewerScBvRecord> {

    /**
     * bv点播过几次了
     */
    fun bvCount(spec: ViewerScBvCountSpec): Long

}
