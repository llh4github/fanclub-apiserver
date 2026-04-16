/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.entity.anchor.AnchorInfo
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorIdExistSpec
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import llh.fanclubvup.common.BID

interface AnchorInfoService : BaseDatabaseService<AnchorInfo> {
    fun checkBid(bid: BID): Boolean = existBySpec(AnchorIdExistSpec(bid))

    fun checkRoomId(roomId: Long): Boolean = existBySpec(AnchorIdExistSpec(roomId = roomId))
}
