/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.consts

import llh.fanclubvup.common.consts.PropsKeys

object BiliSdkCacheKey {
    private const val PREFIX = PropsKeys.APP_PROP_KEY + ":bili_sdk_cache"

    const val WBI_SIGN = "$PREFIX:wbi_sign"

    const val COOKIES = "$PREFIX:cookies"
}