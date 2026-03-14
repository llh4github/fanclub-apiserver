/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto

import com.fasterxml.jackson.annotation.JsonProperty
import llh.fanclubvup.bilisdk.dto.guard.GuardData

/**
 * 大航海列表响应
 */
data class GuardPageResponse(
    @JsonProperty("data")
    val data: GuardData? = null
) : ScraperBaseResp()
