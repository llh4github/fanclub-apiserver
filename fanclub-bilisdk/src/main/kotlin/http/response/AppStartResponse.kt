/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.http.response

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class AppStartResponse(
    @JsonProperty("data")
    val data: ResponseData? = null
) : BaseResponse() {

    data class ResponseData(
        // 场次信息
        @JsonAlias("game_info")
        val gameInfo: GameInfo,

        // 长连信息
        @JsonAlias("websocket_info")
        val websocketInfo: WebsocketInfo,

        // 主播信息
        @JsonAlias("anchor_info")
        val anchorInfo: AnchorInfo
    )

    data class GameInfo(
        /**
         * 场次id
         * 心跳key(推荐心跳保持20s)调用一次
         * 互动玩法超过60秒，H5插件和工具超过180s无心跳自动关闭,长连停止推送消息
         */
        @JsonAlias("game_id")
        val gameId: String
    )

    data class WebsocketInfo(
        /**
         * 长连使用的请求json体
         * 第三方无需关注内容,建立长连时使用即可
         */
        @JsonAlias("auth_body")
        val authBody: String,

        /**
         * wss 长连地址
         * 返回多个不同集群的地址，默认使用第一个即可
         * 用于生产环境请做好连接失败后切换集群的兜底逻辑
         */
        @JsonAlias("wss_link")
        val wssLink: List<String>
    )

    data class AnchorInfo(
        /**
         * 主播房间号
         */
        @JsonAlias("room_id")
        val roomId: Long,

        /**
         * 主播昵称
         */
        @JsonAlias("uname")
        val uname: String,

        /**
         * 主播头像
         */
        @JsonAlias("uface")
        val uface: String,

        /**
         * 主播uid
         */
        @JsonAlias("uid")
        val uid: Long,

        /**
         * 用户唯一标识
         */
        @JsonAlias("open_id")
        val openId: String,

        /**
         * 开发者维度下用户唯一标识
         */
        @JsonAlias("union_id")
        val unionId: String
    )
}