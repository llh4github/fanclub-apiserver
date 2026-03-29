/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.statistics.BiliDanmuStatistics
import llh.fanclubvup.bilisdk.dm.CommandProcessor
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * 弹幕统计服务测试类
 */
@SpringBootTest
@ActiveProfiles("docker")
class BiliDanmuStatisticsTest {
    @Autowired
    private lateinit var service: BiliDanmuStatistics

    @Test
    fun test_UserToastMsgV2Cmd_parse() {
        val json = """
           {"cmd":"USER_TOAST_MSG_V2","data":{"effect_info":{"effect_id":397,"face_effect_id":44,"room_effect_id":590,"room_gift_effect_id":0,"room_group_effect_id":1337,"ship_effect_id":590},"gift_info":{"gift_id":10003},"group_guard_info":null,"guard_info":{"end_time":1773499416,"guard_level":3,"op_type":2,"role_name":"舰长","room_guard_count":14031,"start_time":1773499416},"option":{"anchor_show":true,"color":"#00D1F1","is_group":0,"is_show":0,"source":0,"svga_block":0,"user_show":true},"pay_info":{"num":1,"payflow_id":"2603142242132392111600018","price":168000,"unit":"月"},"receiver_uinfo":{"base":{"face":"https://i2.hdslb.com/bfs/face/619f378852ebac9fdf87e20418d6f99bfa750c7f.jpg","name":"测试主播"},"uid":114514},"sender_uinfo":{"base":{"face":"","name":"测试用户"},"uid":5201314},"toast_msg":"\u003c%测试用户%\u003e 在主播测试主播的直播间开通了舰长，今天是TA陪伴主播的第91天"},"msg_id":"89071851010055680:1000:1000","p_is_ack":true,"p_msg_type":1,"send_time":1773499416623}
       """.trimIndent()
        val cmd = CommandProcessor.parseCommand(json) ?: throw Exception("JSON解析失败")
        service.handleMsg(cmd)
    }
}