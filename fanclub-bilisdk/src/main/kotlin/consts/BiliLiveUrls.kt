package llh.fanclubvup.bilisdk.consts

/**
 * 直播接口
 * [文档](https://open-live.bilibili.com/document/eba8e2e1-847d-e908-2e5c-7a1ec7d9266f#h2-u9879u76EEu5F00u542F)
 */
object BiliLiveUrls {
    private const val BASE_URL = "https://live-open.biliapi.com"

    /**
     * 开启项目第一步，平台会根据入参进行鉴权校验。鉴权通过后，返回长连信息、场次信息和主播信息。
     */
    const val START_APP = "$BASE_URL/v2/app/start"

    /**
     * 项目关闭时需要主动调用此接口，使用对应项目Id及项目开启时返回的game_id作为唯一标识，调用后会同步下线互动道具等内容，项目关闭后才能进行下一场次互动。
     */
    const val END_APP = "$BASE_URL/v2/app/start"
}
