/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import llh.fanclubvup.bilisdk.dto.danmu.DanmuInfoData

/**
 * 弹幕信息响应
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DanmuInfoResponse(
    @JsonProperty("data")
    val data: DanmuInfoData? = null
) : ScraperBaseResp()
