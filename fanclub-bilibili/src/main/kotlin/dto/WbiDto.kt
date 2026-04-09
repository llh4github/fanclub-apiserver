package llh.fanclubvup.bilibili.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
abstract class BiliBaseResponse {
    @JsonProperty("code")
    val code: Int = 0

    @JsonProperty("message")
    val message: String = "成功"
}

data class WbiImgDto(
    @JsonProperty("img_url") val imgUrl: String,
    @JsonProperty("sub_url") val subUrl: String
)

data class WbiData(
    @JsonProperty("wbi_img") val wbiImg: WbiImgDto? = null
)

data class WbiInfoResponse(val data: WbiData? = null) : BiliBaseResponse()

data class DanmuHost(
    val host: String,
    val port: Int,
    @JsonProperty("ws_port") val wsPort: Int,
    @JsonProperty("wss_port") val wssPort: Int
)

data class DanmuInfoData(
    val group: String? = null,
    @JsonProperty("business_id") val businessId: Long? = null,
    @JsonProperty("refresh_row_factor") val refreshRowFactor: Double? = null,
    @JsonProperty("refresh_rate") val refreshRate: Long? = null,
    @JsonProperty("max_delay") val maxDelay: Long? = null,
    val token: String? = null,
    @JsonProperty("host_list") val hostList: List<DanmuHost>? = null
)

data class DanmuInfoResponse(val data: DanmuInfoData? = null) : BiliBaseResponse()
