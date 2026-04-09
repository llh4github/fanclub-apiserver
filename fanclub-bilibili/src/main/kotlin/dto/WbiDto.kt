package llh.fanclubvup.bilibili.dto

data class WbiImgDto(val imgUrl: String, val subUrl: String)
data class WbiData(val wbiImg: WbiImgDto? = null)
data class WbiInfoResponse(val data: WbiData? = null)

data class DanmuHost(val host: String, val port: Int, val wsPort: Int, val wssPort: Int)

data class DanmuInfoData(
    val group: String? = null,
    val businessId: Long? = null,
    val refreshRowFactor: Double? = null,
    val refreshRate: Long? = null,
    val maxDelay: Long? = null,
    val token: String? = null,
    val hostList: List<DanmuHost>? = null
)

data class DanmuInfoResponse(val data: DanmuInfoData? = null)
