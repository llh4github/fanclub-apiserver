/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.props

import llh.fanclubvup.bilibili.dto.SerializableCookie
import llh.fanclubvup.common.BID

/**
 * B站客户端配置接口
 * 由外部实现，提供客户端所需的配置信息
 * 
 * 设计目的：
 * 1. 解耦配置与实现，让配置可以从不同来源获取
 * 2. 统一配置获取方式，方便维护
 * 3. 支持配置的动态刷新
 */
interface BiliClientConfig {
    /**
     * 获取用户 ID
     * 
     * 用户 ID 是 B站 API 调用和 WebSocket 认证的必要参数
     * 
     * @return 用户 ID
     */
    fun getUid(): BID

    /**
     * 获取 buvid
     * 
     * buvid 是 B站用于标识设备的唯一 ID，是 WebSocket 认证的必要参数
     * 
     * @return buvid
     */
    fun getBuvid(): String

    /**
     * 获取 cookie 列表
     * 
     * cookies 用于 HTTP 请求的身份认证，是获取弹幕服务器信息的必要参数
     * 使用 SerializableCookie 而非 okhttp3.Cookie 是为了方便序列化和存储
     * 
     * @return cookie 列表
     */
    fun getCookies(): List<SerializableCookie>

    /**
     * 刷新配置
     * 
     * 当配置来源发生变化时，调用此方法刷新配置
     */
    fun refresh()
}