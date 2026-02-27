/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.http

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.utils.Md5Utils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * B 站直播开放 API 客户端
 *
 *  [接口文档](https://open-live.bilibili.com/document/74eec767-e594-7ddd-6aba-257e8317c05d#h1-u5FEBu901Fu5F00u59CB)
 */
@Component
class BiliLiveOpenApiClient(
    @Qualifier("biliLiveOpenApiRestClient")
    private val restClient: RestClient
) {

    private val logger = KotlinLogging.logger {}

    fun sendRequest(uri: String, bodyJson: String) {
        val md5 = Md5Utils.encode(bodyJson)
        if (md5.isFailure) {
            logger.error { "MD5请求体失败： $bodyJson" }
            return
        }

        val builder = restClient.post().uri(uri)
        val headerBuilder = BiliLiveOpenApiHeaderBuilder(
            "TODO",
            md5.getOrDefault(""),
            ""
        )
        if (headerBuilder.fillHeader(builder)) {
            builder
                .body(bodyJson)
                .retrieve().toBodilessEntity()
        }
    }
}
