/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("docker")
class PasswordTest {
    @Autowired
    private lateinit var service: PasswordEncoder

    /**
     * 测试用密码
     */
    @Test
    fun a() {
        val encode = service.encode("123456qaz")
        println(encode)
    }
}
