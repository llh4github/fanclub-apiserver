package llh.fanclubvup.apiserver.components.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class BiliSignCacheManager(
    private val redisTemplate: StringRedisTemplate,
) {
    private val localCache by lazy {
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(3))
            .expireAfterAccess(Duration.ofMinutes(1))
            .build<String, String>()
    }

    fun get(key: String): String? {
        val tmp = localCache.getIfPresent(key)
        if (tmp != null) {
            return tmp
        }

        redisTemplate.opsForValue().get(key)?.let {
            localCache.put(key, it)
            return it
        }
        return null
    }

    fun set(key: String, value: String, expire: Duration) {
        redisTemplate.opsForValue().set(key, value, expire)
        localCache.put(key, value)
    }
}
