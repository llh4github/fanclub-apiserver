/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.constants

object ApiConstants {
    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    const val WBI_INIT_URL = "https://api.bilibili.com/x/web-interface/nav"
    const val DANMAKU_SERVER_CONF_URL = "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo"
    
    /**
     * 获取用户关系信息（关注数，粉丝数）
     */
    const val USER_RELATION_STAT_API = "https://api.bilibili.com/x/relation/stat"
}
