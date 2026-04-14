package llh.fanclubvup.apiserver.service

import com.pig4cloud.captcha.SpecCaptcha
import com.pig4cloud.captcha.base.Captcha
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("docker")
class CaptchaTest {

    @Test
    fun a() {
        val specCaptcha = SpecCaptcha(130, 48, 5)
        specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER)
        val text = specCaptcha.text()
        println(text)
//        specCaptcha.out(File("").outputStream())
    }
}
