package llh.fanclubvup.bilisdk.scraper

interface BiliWsAuthFetcher {

    fun fetch(roomId: Long): String?
}
