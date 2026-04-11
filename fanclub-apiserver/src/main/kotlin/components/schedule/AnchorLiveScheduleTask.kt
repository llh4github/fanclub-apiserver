/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.schedule

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.dto.ai.AnchorLiveScheduleItemTypeRef
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorLiveScheduleAddInput
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveScheduleService
import llh.fanclubvup.apiserver.utils.JsonUtils
import llh.fanclubvup.bilibili.constants.ApiConstants
import llh.fanclubvup.common.consts.CacheKeyPrefix
import llh.fanclubvup.common.consts.PropsKeys.BILI_DYN_SCHEDULER_LIKO
import org.jsoup.Jsoup
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.content.Media
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.ResponseFormat
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils
import java.net.URI
import java.time.LocalDate

/**
 * 直播日程定时任务
 *
 * token调用贵，这里先不考虑通用实现
 */
@Component
class AnchorLiveScheduleTask(
    private val service: AnchorLiveScheduleService,
    private val redisTemplate: StringRedisTemplate,
    private val chatModel: OpenAiChatModel,
) {
    private val logger = KotlinLogging.logger { }

    /**
     * 解析日程数据属于BID
     */
    val belongBid = 1536601294L

    @Value($$"${$$BILI_DYN_SCHEDULER_LIKO:''}")
    private lateinit var dynUrl: String

    val promptStr = """
    分析图片中的日程信息，提取结构化数据。
    要求：
    1. 如果图片中没有日程信息，则忽略
    2. 提取每个日程的：项目名称、开始时间、结束时间
    3. 时间格式：`yyyy-MM-dd HH:mm:ss`
    4. 如果没有结束时间，则设为开始时间后2小时
    5. 忽略"休"、"旅途"等没有具体时间的项目
    6. 所有图片都没有日程信息，则返回`[]`
    """.trimIndent()

    @Scheduled(cron = "0 0 6 * * ?")
    fun syncLiveSchedule() {
        if (dynUrl.isEmpty()) {
            logger.warn { "B站动态链接为空, 请检查配置项: $BILI_DYN_SCHEDULER_LIKO" }
            return
        }
            
        val has = service.hasSchedule(belongBid, LocalDate.now())
        if (has) {
            logger.info { "已有本周日程数据，跳过执行" }
            return
        }
            
        parseAndSaveSchedule()
    }

    /**
     * 解析并保存直播日程
     */
    private fun parseAndSaveSchedule() {
        val imageUrls = fetchDynamicImages()
        if (imageUrls.isEmpty()) {
            logger.info { "未找到符合条件的图片" }
            return
        }

        val uncheckImg = filterUnprocessedImages(imageUrls)
        if (uncheckImg.isEmpty()) {
            logger.info { "所有图片已处理过，无需重复解析" }
            return
        }

        parseAndSaveWithAI(uncheckImg)
        redisTemplate.opsForSet().add(CacheKeyPrefix.DYN_IMG_SET_LIKO, *uncheckImg.toTypedArray())
    }

    /**
     * 从B站动态页面提取图片URL
     */
    private fun fetchDynamicImages(): List<String> {
        val doc = Jsoup.connect(dynUrl)
            .userAgent(ApiConstants.USER_AGENT)
            .timeout(10 * 1000)
            .get()

        return doc.select("img")
            .map { it.absUrl("src") }
            .filter { url ->
                url.contains("bfs/new_dyn/") &&
                        url.contains("@") &&
                        url.contains(".png")
            }
            .map { it.substringBefore("@") }
            .distinct()
    }

    /**
     * 过滤未处理的图片
     */
    private fun filterUnprocessedImages(imageUrls: List<String>): List<String> {
        val rs = redisTemplate.opsForSet().isMember(
            CacheKeyPrefix.DYN_IMG_SET_LIKO,
            *imageUrls.toTypedArray()
        )
        return rs.filter { !it.value }
            .map { it.key as String }
    }

    /**
     * 使用AI解析图片并保存日程数据
     */
    private fun parseAndSaveWithAI(imgUrls: List<String>) {
        val medias = imgUrls.map { Media(MimeTypeUtils.IMAGE_PNG, URI.create(it)) }
        val mapper = JsonUtils.mapper
        val outputConverter = BeanOutputConverter(AnchorLiveScheduleItemTypeRef, mapper)
        val jsonSchema = outputConverter.jsonSchema
        
        val userMessage = UserMessage.builder()
            .text(promptStr)
            .media(*medias.toTypedArray())
            .build()
        
        val response = chatModel.call(
            Prompt(
                userMessage,
                OpenAiChatOptions.builder()
                    .responseFormat(ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                    .build()
            )
        )

        response.result?.output?.text?.let { jsonText ->
            val scheduleItems = outputConverter.convert(jsonText)
            val addInputs = scheduleItems.map { item ->
                AnchorLiveScheduleAddInput(
                    bid = belongBid,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    topic = item.topic,
                )
            }
            
            val savedCount = service.saveList(addInputs)
            logger.info { "AI解析出 ${scheduleItems.size} 条日程数据，成功保存 $savedCount 条" }
        }
    }
}
