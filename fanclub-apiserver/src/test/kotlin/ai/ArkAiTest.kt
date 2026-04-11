/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.ai

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.content.Media
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.MimeTypeUtils
import java.net.URI
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
}