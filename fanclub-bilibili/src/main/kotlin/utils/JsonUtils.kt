package llh.fanclubvup.bilibili.utils

import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.jsonMapper

/**
 * 统一的 JSON Mapper 单例
 */
object JsonUtils {
    val mapper by lazy {
        jsonMapper {
            addModule(KotlinModule.Builder().build())
        }
    }
}
