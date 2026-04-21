package llh.fanclubvup.apiserver.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.bilibili.constants.ApiConstants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * 第三方API HTTP客户端示例
 *
 */
class ThirdPartyHttpClient(
    private val baseUrl: String,
    private val apiKey: String? = null,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * OkHttp 客户端实例
     * 配置超时时间和拦截器
     */
    private val client by lazy {
        val builder = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)


        // 可选：添加认证头
        apiKey?.let { key ->
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $key")
                    .build()
                chain.proceed(request)
            }
        }

        builder.build()
    }

    /**
     * JSON 映射器（使用项目统一的工具类）
     */
    private val mapper = JsonUtils.mapper

    /**
     * 执行 GET 请求
     *
     * @param path 请求路径（相对路径）
     * @param clazz 响应类型
     * @return 响应结果
     */
    fun <T> executeGet(path: String, clazz: Class<T>): Result<T> {
        return try {
            val url = "$baseUrl$path"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", ApiConstants.USER_AGENT)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }

                val body = response.body.string()
                val result = mapper.readValue(body, clazz)
                Result.success(result)
            }
        } catch (e: Exception) {
            logger.error(e) { "GET 请求失败: $path" }
            Result.failure(e)
        }
    }

    /**
     * 执行 POST 请求
     *
     * @param path 请求路径
     * @param requestBody 请求体对象
     * @param clazz 响应类型
     * @return 响应结果
     */
    fun <T, R> executePost(path: String, requestBody: T, clazz: Class<R>): Result<R> {
        return try {
            val url = "$baseUrl$path"
            val jsonBody = mapper.writeValueAsString(requestBody)

            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("User-Agent", ApiConstants.USER_AGENT)
                .post(
                    jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
                )
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }

                val body = response.body.string()
                val result = mapper.readValue(body, clazz)
                Result.success(result)
            }
        } catch (e: Exception) {
            logger.error(e) { "POST 请求失败: $path" }
            Result.failure(e)
        }
    }

    /**
     * 执行带查询参数的 GET 请求
     *
     * @param path 请求路径
     * @param params 查询参数
     * @param clazz 响应类型
     * @return 响应结果
     */
    fun <T> executeGetWithParams(path: String, params: Map<String, String>, clazz: Class<T>): Result<T> {
        val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val fullPath = if (queryString.isNotEmpty()) "$path?$queryString" else path
        return executeGet(fullPath, clazz)
    }
}
