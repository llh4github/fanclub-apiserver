/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.enums

/**
 * B站API接口URL枚举类
 */
@Deprecated("移动到sdk模块里")
enum class BiliURLs(val url: String) {
    /** 用户ID初始化URL - 获取用户导航信息 */
    UID_INIT_URL("https://api.bilibili.com/x/web-interface/nav"),

    /** WBI初始化URL - 获取WBI签名参数 */
    WBI_INIT_URL("https://api.bilibili.com/x/web-interface/nav"),

    /** BUVID初始化URL - 获取浏览器唯一标识 */
    BUVID_INIT_URL("https://www.bilibili.com/"),

    /** 直播间初始化URL - 获取直播间基本信息 */
    ROOM_INIT_URL("https://api.live.bilibili.com/room/v1/Room/get_info"),

    /** 弹幕服务器配置URL - 获取弹幕服务器连接信息 */
    DANMAKU_SERVER_CONF_URL("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo")
}