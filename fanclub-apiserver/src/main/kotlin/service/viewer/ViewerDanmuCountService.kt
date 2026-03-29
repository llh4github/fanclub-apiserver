package llh.fanclubvup.apiserver.service.viewer

import llh.fanclubvup.apiserver.entity.viewer.ViewerDanmuCount
import llh.fanclubvup.apiserver.entity.viewer.dto.ViewerDanmuCountAddInput
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface ViewerDanmuCountService : BaseDatabaseService<ViewerDanmuCount> {

    fun saveListNoTx(list: List<ViewerDanmuCountAddInput>): Int

}
