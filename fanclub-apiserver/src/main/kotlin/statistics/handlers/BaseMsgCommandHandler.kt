/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.statistics.handlers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.Executors

abstract class BaseMsgCommandHandler {
    @Autowired
    protected lateinit var redisTemplate: StringRedisTemplate

    protected val executors = Executors.newVirtualThreadPerTaskExecutor()
}