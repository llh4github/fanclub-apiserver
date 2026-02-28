/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.JsonNode

@JsonIgnoreProperties(ignoreUnknown = true)
data class UnknownCommand(
    @JsonProperty("cmd") override val cmd: String,
    @field:JsonIgnore val rawData: Map<String, JsonNode> = emptyMap()
) : Command() {

    // 使用内联函数安全获取字段
    inline fun <reified T> getField(key: String): T? {
        return rawData[key]?.let { node ->
            when (T::class) {
                String::class -> node.asString() as? T
                Long::class -> node.asLong() as? T
                Int::class -> node.asInt() as? T
                Boolean::class -> node.asBoolean() as? T
                Double::class -> node.asDouble() as? T
                else -> null
            }
        }
    }

    fun getField(key: String): JsonNode? = rawData[key]
}