/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.service.anchor.AnchorLiveDurationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("docker")
class AnchorLiveDurationServiceTest {

    @Autowired
    lateinit var service: AnchorLiveDurationService


    // 需要先建立测试数据
    @Test
    fun test_computeLiveDuration() {
        val liveDuration = service.computeLiveDuration(
            roomId = 1, date = LocalDate.now()
        )
        println(liveDuration)
    }
}
