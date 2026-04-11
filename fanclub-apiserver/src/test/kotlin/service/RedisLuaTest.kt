/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("docker")
class RedisLuaTest {

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @Autowired
    @Qualifier("deleteByPattern")
    private lateinit var deleteByPattern: DefaultRedisScript<Long>

    @Test
    fun test() {
    }

    @Test
    fun test_deleteByPattern() {
        val size = 10
        val key = "test-delete:324"
        val keyPrefix = "test-delete:sub"
        redisTemplate.opsForValue().set(key, "123")
        for (i in 1..10) {
            redisTemplate.opsForValue().set("$keyPrefix:$i", "123")
        }

        val keys = listOf(
            key,
            "$keyPrefix:*",
        )
        val deleted = redisTemplate.execute(deleteByPattern, keys, "")
        assertEquals(size + 1, deleted.toInt())
    }
}
