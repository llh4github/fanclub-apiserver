/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.dto.viwer


/**
 * 弹幕消息
 *
 * @param sender 发送者
 * @param content 内容
 * @param roomId 房间ID
 * @param level 等级
 */
data class DanmuWsMsg(
    val sender: String,
    val content: String,
    val roomId: Long,
    val level: Int,
)
