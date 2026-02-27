/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.http

import okhttp3.OkHttpClient
import okhttp3.Request

class BiliLiveApiClient {
    private val client = OkHttpClient()

    fun a(prop: BiliLiveApiHeaderBuilder) {
        val request = Request.Builder()
        if (prop.fillHeader(request)) {

        }

    }
}
