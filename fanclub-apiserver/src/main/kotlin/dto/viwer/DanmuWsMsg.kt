/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.viwer

import llh.fanclubvup.common.BID


data class DanmuWsMsg(
    val sender: String,
    val content: String,
    val targetUID: BID,
    val level: Int,
)
