/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.ai

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import llh.fanclubvup.common.consts.DatetimeConstant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.content.Media
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.ResponseFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.MimeTypeUtils
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.net.URI
import java.time.LocalDateTime
import kotlin.test.Test


@SpringBootTest
@ActiveProfiles("docker")
class ArkAiTest {
    @Autowired
    private lateinit var chatModel: OpenAiChatModel

    @Test
    fun test_text_chat() {
        val response = chatModel.call("介绍下你自己")
        println(response)
    }

    /**
     * 注：只支持图片链接和base64
     */
    @Test
    fun test_image_read() {
        val uri = URI.create("https://ark-project.tos-cn-beijing.volces.com/images/view.jpeg")
        val userMessage: UserMessage = UserMessage.builder()
            .text("图片主要讲了什么?") // content
            .media(Media(MimeTypeUtils.IMAGE_PNG, uri)) // media
            .build()
        val response = chatModel.call(Prompt(userMessage))
        println(response)
    }

    data class ScheduleItem(
        @JsonProperty(required = true, value = "item")
        val item: String,
        @JsonProperty(required = true, value = "startTime")
        @JsonFormat(pattern = DatetimeConstant.DATE_TIME_FORMAT, timezone = "GMT+8")
        val startTime: LocalDateTime,
        @JsonProperty(required = true, value = "endTime")
        @JsonFormat(pattern = DatetimeConstant.DATE_TIME_FORMAT, timezone = "GMT+8")
        val endTime: LocalDateTime
    )


    /**
     * 测试结构化输出
     */
    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://i0.hdslb.com/bfs/new_dyn/7dd96ad8582e378dcb657a3607969b741536601294.png",
            "https://i2.hdslb.com/bfs/new_dyn/9f53ed4a0a849d4c4a6d2ce1e5228a321536601294.png"
        ]
    )
    fun test_schedule_picture_read(url: String) {

        val uri = URI.create(url)
        val mapper = jsonMapper { addModule(kotlinModule()) }
        val rty = object : ParameterizedTypeReference<List<ScheduleItem>>() {}
        val outputConverter = BeanOutputConverter(rty, mapper)
        val jsonSchema = outputConverter.jsonSchema
        println(jsonSchema)
        val userMessage: UserMessage = UserMessage.builder()
            .text(
                """
    分析图片中的日程信息，提取结构化数据。
    要求：
    1. 如果图片中没有日程信息，返回：`[]`
    2. 提取每个日程的：项目名称、开始时间、结束时间
    3. 时间格式：`yyyy-MM-dd HH:mm:ss`
    4. 如果没有结束时间，则设为开始时间后2小时
    5. 忽略"休"、"旅途"等没有具体时间的项目
            """.trimIndent()
            )
            .media(Media(MimeTypeUtils.IMAGE_PNG, uri))
            .build()
        val response = chatModel.call(
            Prompt(
                userMessage,
                OpenAiChatOptions.builder()
                    .responseFormat(ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                    .build()
            )
        )
        println(response)
        response.result?.output?.text?.let {
            val rs = outputConverter.convert(it)
            println(rs)
        }
    }
}