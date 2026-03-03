/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.http

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilisdk.consts.BiliLiveUrls
import llh.fanclubvup.bilisdk.props.BiliLiveApiProp
import llh.fanclubvup.bilisdk.response.AppStartResponse
import llh.fanclubvup.bilisdk.response.BaseResponse
import llh.fanclubvup.common.excptions.AppRuntimeException
import llh.fanclubvup.common.utils.Md5Utils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tools.jackson.module.kotlin.jacksonObjectMapper

class BiliLiveApiClient(private val prop: BiliLiveApiProp) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val logger = KotlinLogging.logger {}
    private val contentType = "application/json".toMediaType()

    /**
     * 开启项目第一步，平台会根据入参进行鉴权校验。鉴权通过后，返回长连信息、场次信息和主播信息。
     * @param code 主播身份码
     */
    fun appStart(code: String) = execute(
        url = BiliLiveUrls.START_APP,
        contentJson =
            """{"code":"$code", "app_id":${prop.appId}}""".trimIndent(),
        rs = AppStartResponse::class.java
    )

    fun appEnd(gameId: String) = execute(
        url = BiliLiveUrls.END_APP,
        contentJson =
            """{"game_id":"$gameId", "app_id":${prop.appId}}""".trimIndent(),
        rs = BaseResponse::class.java
    )

    private fun <T : BaseResponse> execute(url: String, contentJson: String, rs: Class<T>): Result<T> =
        runCatching {
            val md5 = Md5Utils.encode(contentJson).getOrThrow()
            val request = Request.Builder()
            val header = BiliLiveApiHeaderBuilder(
                accessKeyId = prop.accessKeyId,
                signatureKey = prop.signatureKey,
                contentMd5 = md5
            )
            if (header.fillHeader(request)) {
                client.newCall(
                    request
                        .post(contentJson.toRequestBody(contentType))
                        .url(url)
                        .build()
                ).execute().use { response ->
                    logger.debug { response }
                    if (!response.isSuccessful) {
                        logger.error { "请求失败 $response" }
                        throw AppRuntimeException("请求失败")
                    }
                    val rs = mapper.readValue(response.body.string(), rs)
                    return@runCatching rs
                }
            } else {
                logger.error { "请求头填充失败" }
                throw AppRuntimeException("请求头填充失败")
            }
        }
}
