/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.statistics.BiliDanmuStatistics
import llh.fanclubvup.bilisdk.dm.CommandProcessor
import llh.fanclubvup.bilisdk.dm.cmd.LiveCommand
import llh.fanclubvup.bilisdk.dm.cmd.SuperChatCommand
import llh.fanclubvup.bilisdk.dm.cmd.UserToastMsgV2Cmd
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

/**
 * 弹幕统计服务测试类
 */
@SpringBootTest
@ActiveProfiles("docker")
class BiliDanmuStatisticsTest {
    @Autowired
    private lateinit var service: BiliDanmuStatistics
    private val roomId: Long = 111

    @Test
    fun test_UserToastMsgV2Cmd_parse() {
        val json = """
           {"cmd":"USER_TOAST_MSG_V2","data":{"effect_info":{"effect_id":397,"face_effect_id":44,"room_effect_id":590,"room_gift_effect_id":0,"room_group_effect_id":1337,"ship_effect_id":590},"gift_info":{"gift_id":10003},"group_guard_info":null,"guard_info":{"end_time":1773499416,"guard_level":3,"op_type":2,"role_name":"舰长","room_guard_count":14031,"start_time":1773499416},"option":{"anchor_show":true,"color":"#00D1F1","is_group":0,"is_show":0,"source":0,"svga_block":0,"user_show":true},"pay_info":{"num":1,"payflow_id":"2603142242132392111600018","price":168000,"unit":"月"},"receiver_uinfo":{"base":{"face":"https://i2.hdslb.com/bfs/face/619f378852ebac9fdf87e20418d6f99bfa750c7f.jpg","name":"测试主播"},"uid":114514},"sender_uinfo":{"base":{"face":"","name":"测试用户"},"uid":5201314},"toast_msg":"\u003c%测试用户%\u003e 在主播测试主播的直播间开通了舰长，今天是TA陪伴主播的第91天"},"msg_id":"89071851010055680:1000:1000","p_is_ack":true,"p_msg_type":1,"send_time":1773499416623}
       """.trimIndent()
        val cmd = CommandProcessor.parseCommand(json) as? UserToastMsgV2Cmd ?: throw Exception("JSON解析失败")
        service.handleMsg(cmd, roomId)
    }

    @Test
    fun test_LiveCommand() {
        val json = """
            {"cmd":"LIVE","live_key":"685442366582205620","voice_background":"http://i0.hdslb.com/bfs/live/live_room_skin/071b186fc69d448332c2473fb0238b0ff85ef7e4.jpg","sub_session_key":"685442366582205620sub_time:1773842193","live_platform":"android_link","live_model":3,"roomid":1713548468,"live_time":1773842193,"special_types":[]}
       """.trimIndent()
        val cmd = CommandProcessor.parseCommand(json) as? LiveCommand ?: throw Exception("JSON解析失败")
        service.handleMsg(cmd, roomId)
    }

    @Test
    fun test_SUPER_CHAT_MESSAGE() {
        val json = """
           {"cmd":"SUPER_CHAT_MESSAGE","data":{"send_time":1774933668058,"dmscore":616,"end_time":1774878446,"id":15076419,"is_mystery":false,"is_ranked":0,"is_send_audit":0,"medal_info":{"anchor_roomid":1713548468,"anchor_uname":"莉蔻Liko","guard_level":0,"medal_level":21,"medal_name":"蔻萝特","target_id":1536601294},"message":"测试BV1BrDiBZENn SC内容","price":30,"rate":1000,"start_time":1774878386,"time":60,"token":"E6739D0C","trans_mark":0,"ts":1774878386,"uid":114514,"user_info":{"face":"https://i2.hdslb.com/bfs/face/f73c9be5ced19cfbe5319a196547062dd01ab088.jpg","guard_level":0,"is_main_vip":1,"is_svip":0,"is_vip":0,"manager":0,"uname":"测试用户名","user_level":25}},"msg_id":"90517806075892740:1000:1000","send_time":1774878386832}
       """.trimIndent()
        val cmd = CommandProcessor.parseCommand(json) as? SuperChatCommand ?: throw Exception("JSON解析失败")
        service.handleMsg(cmd, roomId)
        TimeUnit.SECONDS.sleep(2)
    }
}
