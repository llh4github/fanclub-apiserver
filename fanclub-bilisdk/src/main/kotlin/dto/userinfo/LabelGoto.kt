/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 标签跳转链接
 * 示例数据：
 * {
 *   "mobile": "https://big.bilibili.com/mobile/index?navhide=1&from_spmid=vipicon",
 *   "pc_web": "https://account.bilibili.com/big?from_spmid=vipicon"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LabelGoto(
    /**
     * 移动端跳转链接，示例：https://big.bilibili.com/mobile/index?navhide=1&from_spmid=vipicon
     */
    @JsonProperty("mobile")
    val mobile: String? = null,
    /**
     * PC端跳转链接，示例：https://account.bilibili.com/big?from_spmid=vipicon
     */
    @JsonProperty("pc_web")
    val pcWeb: String? = null
)