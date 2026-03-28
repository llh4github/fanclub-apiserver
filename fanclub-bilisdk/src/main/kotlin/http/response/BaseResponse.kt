/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.http.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
abstract class BaseResponse {

    /**
     * 返回码
     */
    @JsonProperty("code")
    val code: Int = 0

    /**
     * 返回信息
     */
    @JsonProperty("message")
    val message: String = "成功"

    /**
     * 请求id
     */
    @JsonProperty("request_id")
    val requestId: String = ""

}
