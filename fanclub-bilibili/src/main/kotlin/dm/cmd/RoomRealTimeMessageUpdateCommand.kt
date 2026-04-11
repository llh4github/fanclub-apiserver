/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 房间实时消息更新命令
 * 用于解析 "ROOM_REAL_TIME_MESSAGE_UPDATE" 类型的命令
 * 包含房间粉丝数、粉丝团人数等实时数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RoomRealTimeMessageUpdateCommand(
    /**
     * 命令类型
     */
    override val cmd: String = "ROOM_REAL_TIME_MESSAGE_UPDATE",
    
    /**
     * 房间实时消息数据
     */
    @JsonProperty("data")
    val data: RoomRealTimeData? = null
) : Command {
    
    /**
     * 房间实时消息数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RoomRealTimeData(
        /**
         * 房间 ID
         */
        @JsonProperty("roomid")
        val roomId: Long? = null,
        
        /**
         * 粉丝数量
         */
        @JsonProperty("fans")
        val fans: Int? = null,
        
        /**
         * 红点通知状态
         * -1: 无通知, 其他值: 有通知
         */
        @JsonProperty("red_notice")
        val redNotice: Int? = null,
        
        /**
         * 粉丝团人数
         */
        @JsonProperty("fans_club")
        val fansClub: Int? = null
    )
}
