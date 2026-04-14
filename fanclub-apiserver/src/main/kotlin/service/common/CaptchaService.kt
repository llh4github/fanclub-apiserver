package llh.fanclubvup.apiserver.service.common

import com.pig4cloud.captcha.SpecCaptcha
import llh.fanclubvup.apiserver.dto.CaptchaData
import llh.fanclubvup.apiserver.utils.IdGenerator
import llh.fanclubvup.common.consts.CacheKeyPrefix
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CaptchaService(
    private val redisTemplate: StringRedisTemplate
) {

    /**
     * 生成验证码
     * @param len 验证码长度 , 最小为 4
     * @param ttl 验证码有效期
     * @return CaptchaData
     */
    fun generateCaptcha(
        len: Int = 5,
        ttl: Duration = Duration.ofMinutes(5)
    ): CaptchaData {
        val len = if (len < 4) 4 else len
        val specCaptcha = SpecCaptcha(130, 48, len)
        val key = IdGenerator.nextId()
        val answer = specCaptcha.text()
        redisTemplate.opsForValue().set("${CacheKeyPrefix.CAPTCHA_KEY}$key", answer, ttl)
        return CaptchaData(key, specCaptcha.toBase64())
    }

    /**
     * 验证验证码
     * @param key 验证码 key
     * @param answer 验证码答案
     * @return Boolean
     */
    fun validCaptcha(key: Long, answer: String): Boolean {
        val realAnswer = redisTemplate.opsForValue().get("${CacheKeyPrefix.CAPTCHA_KEY}$key") ?: return false
        return realAnswer == answer
    }
}
