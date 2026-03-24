package llh.fanclubvup.apiserver.components

import llh.fanclubvup.apiserver.consts.CacheKeyPrefix
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.scripting.support.ResourceScriptSource
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration


@EnableCaching
@Configuration
class RedisCacheConfig {

    private val mapper = jacksonObjectMapper()

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): RedisCacheManager {
        // 默认配置
        val config: RedisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(2)) // 默认缓存 2 小时
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(StringRedisSerializer())
            ) // key 序列化
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(GenericJacksonJsonRedisSerializer(mapper))
            ) // value 序列化
            .disableCachingNullValues() // 不缓存 null 值
            .computePrefixWith { cacheName -> CacheKeyPrefix.SERVICE_CACHE_KEY + cacheName + ":" }
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build()
    }

    @Bean
    fun luaResourcesRegistration(): Array<String> {
        // 使用 PathMatchingResourcePatternResolver 自动扫描所有 Lua 文件
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("classpath*:lua/*.lua")

        // 返回所有 Lua 文件的路径（用于 Native 镜像资源注册）
        //FIXME 这种方法还得测试下
        return resources.map { it.uri.toString() }.toTypedArray()
    }

    @Bean("statisticsDanmu")
    fun statisticsDanmu(): DefaultRedisScript<Boolean> {
        val s = DefaultRedisScript<Boolean>()
        s.resultType = Boolean::class.java
        s.setScriptSource(ResourceScriptSource(ClassPathResource("lua/statistics_danmu.lua")))
        return s
    }
}
