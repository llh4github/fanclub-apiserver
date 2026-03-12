package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.entity.anchor.AnchorFollowerNum
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNumQuerySpec
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import llh.fanclubvup.common.BID

interface AnchorFollowerNumService : BaseDatabaseService<AnchorFollowerNum> {
    /**
     * 查询指定时间段内粉丝数
     */
    fun queryNum(spec: AnchorFollowerDateNumQuerySpec): Int

}
