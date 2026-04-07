package llh.fanclubvup.apiserver.service.viewer

import llh.fanclubvup.apiserver.entity.viewer.ViewerScBvRecord
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewScBvCountSpec
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface ViewerScBvRecordService : BaseDatabaseService<ViewerScBvRecord> {

    /**
     * bv点播过几次了
     */
    fun bvCount(spec: ViewScBvCountSpec): Long

}
