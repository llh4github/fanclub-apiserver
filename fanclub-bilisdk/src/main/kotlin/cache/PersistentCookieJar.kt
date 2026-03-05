package llh.fanclubvup.bilisdk.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

object PersistentCookieJar : CookieJar {

    //    private val cacheManager: BiliSignCacheManager
    private val logger = KotlinLogging.logger {}

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        Cookie.Builder()
            .name("name")
            .value("value")
            .domain("domain")
            .build()
        logger.info { "saveFromResponse: $url, $cookies" }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        logger.info { "loadForRequest: $url" }
        return emptyList()
    }
}
