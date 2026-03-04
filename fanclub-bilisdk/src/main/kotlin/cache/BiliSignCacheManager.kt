package llh.fanclubvup.bilisdk.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

class BiliSignCacheManager(private val redisTemplate: StringRedisTemplate) {
    private val localCache by lazy {
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(3))
            .expireAfterAccess(Duration.ofMinutes(1))
            .build<String, String>()
    }
    private val defaultExpire = Duration.ofHours(10)

    fun get(key: String, expire: Duration = defaultExpire, compute: () -> String?): String? {
        val tmp = localCache.getIfPresent(key)
        if (tmp != null) {
            return tmp
        }

        redisTemplate.opsForValue().get(key)?.let {
            localCache.put(key, it)
            return it
        }
        return compute()?.apply {
            set(key, this, expire)
        }

    }

    fun set(key: String, value: String, expire: Duration = defaultExpire) {
        redisTemplate.opsForValue().set(key, value, expire)
        localCache.put(key, value)
    }
}
