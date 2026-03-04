/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.consts

/**
 * Bilibili API 相关 URL 常量
 */
object BiliApiUrls {

    /**
     * 用户 UID 初始化接口
     */
    const val UID_INIT_URL = "https://api.bilibili.com/x/web-interface/nav"

    /**
     * WBI 签名初始化接口（与 UID_INIT_URL 相同）
     */
    const val WBI_INIT_URL = UID_INIT_URL

    /**
     * BUVID 初始化接口
     */
    const val BUVID_INIT_URL = "https://www.bilibili.com/"

    /**
     * 直播间信息初始化接口
     */
    const val ROOM_INIT_URL = "https://api.live.bilibili.com/room/v1/Room/get_info"

    /**
     * 弹幕服务器配置接口
     */
    const val DANMAKU_SERVER_CONF_URL = "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo"
}
