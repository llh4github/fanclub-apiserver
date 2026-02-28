package llh.fanclubvup.bilisdk.response

import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.Test

class ResponseTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun a() {
        // JSON 字符串示例
        val json = """
{
    "code": 0,
    "message": "ok",
    "data": {
        "game_info": {
            "game_id": "game_123456"
        },
        "websocket_info": {
            "auth_body": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
            "wss_link": [
                "wss://ws1.example.com",
                "wss://ws2.example.com"
            ]
        },
        "anchor_info": {
            "room_id": 12345678,
            "uname": "主播小可爱",
            "uface": "https://example.com/avatar.jpg",
            "uid": 100000001,
            "open_id": "39b8fedb-60a5-4e29-ac75-b16955f7e632",
            "union_id": "U_ed2cbbb8-2c8f-4fcd-bc47-b62f0e1fca75"
        }
    }
}
""".trim()
        val resp = mapper.readValue<AppStartResponse>(json, AppStartResponse::class.java)
        println(resp)
    }
}