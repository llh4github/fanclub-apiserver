/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilibili.props

import llh.fanclubvup.bilibili.dto.SerializableCookie
import llh.fanclubvup.common.BID

/**
 * B站客户端配置数据类
 * 持有客户端所需的配置信息
 * 
 * 设计目的：
 * 1. 集中管理配置数据，简化配置传递
 * 2. 利用 data class 的不可变性，保证配置线程安全
 * 3. 提供默认值，支持部分配置缺失的场景
 * 
 * @property uid 用户 ID，B站 API 调用和 WebSocket 认证的必要参数
 * @property buvid 设备唯一标识，WebSocket 认证的必要参数
 * @property cookies Cookie 列表，HTTP 请求身份认证所需
 */
data class BiliClientConfig(
    val uid: BID = -1L,
    val buvid: String = "",
    val cookies: List<SerializableCookie> = emptyList()
) {
    /**
     * 刷新配置
     * 
     * 由于 data class 是不可变的，刷新需要创建新的实例
     * 此方法保留以兼容原有接口设计，实际使用时应创建新实例
     */
    @Deprecated("请创建新的实例")
    fun refresh(): BiliClientConfig {
        // data class 为不可变对象，返回自身或可创建新实例
        return this
    }
}