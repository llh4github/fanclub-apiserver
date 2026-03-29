/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * 直播间实时消息更新命令
 */
@JsonTypeName("ROOM_REAL_TIME_MESSAGE_UPDATE")
@JsonIgnoreProperties(ignoreUnknown = true)
data class RoomRealTimeMessageUpdateCommand(
    @field:JsonProperty("data")
    val data: RoomRealTimeData? = null
) : Command() {

    /**
     * 直播间实时数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RoomRealTimeData(
        @field:JsonProperty("roomid")
        val roomId: Long? = null,

        /**
         * 粉丝数
         */
        @field:JsonProperty("fans")
        val fans: Int? = null,

        @field:JsonProperty("red_notice")
        val redNotice: Int? = null,

        /**
         * 粉丝团数量
         */
        @field:JsonProperty("fans_club")
        val fansClub: Int? = null
    )
}
