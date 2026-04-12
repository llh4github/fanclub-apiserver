/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.ai

import llh.fanclubvup.apiserver.components.schedule.AnchorLiveScheduleTask
import llh.fanclubvup.apiserver.dto.ai.AnchorLiveScheduleItem
import llh.fanclubvup.apiserver.service.anchor.AnchorLiveScheduleService
import llh.fanclubvup.apiserver.utils.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("docker")
class AnchorLiveScheduleTaskTest {
    @Autowired
    private lateinit var task: AnchorLiveScheduleTask

    @Autowired
    private lateinit var service: AnchorLiveScheduleService

    @Test
    fun test_syncLiveSchedule() {
        task.syncLiveSchedule()
    }

    @Test
    fun a() {
        val json = """
            {"endTime":"2024040613:00:00","startTime":"2024040611:00:00","topic":"午间杂谈"}
        """.trimIndent()
        val rs = JsonUtils.mapper.readValue(json, AnchorLiveScheduleItem::class.java)
        println(rs)
    }
}
